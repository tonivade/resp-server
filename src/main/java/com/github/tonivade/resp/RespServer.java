/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.util.Precondition.checkNonNull;
import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import java.io.IOException;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.command.DefaultRequest;
import com.github.tonivade.resp.command.DefaultSession;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.command.Session;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;
import com.github.tonivade.resp.protocol.RedisDecoder;
import com.github.tonivade.resp.protocol.RedisEncoder;
import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.SafeString;
import com.github.tonivade.resp.util.Recoverable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;

public class RespServer implements Resp {

  private static final Logger LOGGER = LoggerFactory.getLogger(RespServer.class);

  private static final int BUFFER_SIZE = 1024 * 1024;
  private static final int MAX_FRAME_SIZE = BUFFER_SIZE * 100;

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 12345;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private ChannelFuture future;

  private final RespServerContext serverContext;

  public RespServer(RespServerContext serverContext) {
    this.serverContext = checkNonNull(serverContext);
  }

  public static Builder builder() {
    return new Builder();
  }

  public void start() {
    bossGroup = new NioEventLoopGroup();
    workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);

    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new RespInitializerHandler(this))
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .childOption(ChannelOption.SO_RCVBUF, BUFFER_SIZE)
        .childOption(ChannelOption.SO_SNDBUF, BUFFER_SIZE)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

    future = bootstrap.bind(serverContext.getHost(), serverContext.getPort());
    // Bind and start to accept incoming connections.
    future.syncUninterruptibly();

    serverContext.start();

    LOGGER.info("server started: {}:{}", serverContext.getHost(), serverContext.getPort());
  }

  public String getHost() {
    return serverContext.getHost();
  }

  public int getPort() {
    return serverContext.getPort();
  }

  public void stop() {
    try {
      if (future != null) {
        closeFuture(future.channel().close());
      }
      future = null;
    } finally {
      workerGroup = closeWorker(workerGroup);
      bossGroup = closeWorker(bossGroup);
    }

    serverContext.stop();

    LOGGER.info("server stopped");
  }

  @Override
  public void channel(SocketChannel channel) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("new channel: {}", sourceKey(channel));
    }

    channel.pipeline().addLast("redisEncoder", new RedisEncoder());
    channel.pipeline().addLast("linDelimiter", new RedisDecoder(MAX_FRAME_SIZE));
    channel.pipeline().addLast(new IdleStateHandler(0, 0, 5 * 60));
    channel.pipeline().addLast(new RespConnectionHandler(this));
  }

  @Override
  public void connected(ChannelHandlerContext ctx) {
    String sourceKey = sourceKey(ctx.channel());
    LOGGER.debug("client connected: {}", sourceKey);
    getSession(ctx, sourceKey);
  }

  @Override
  public void disconnected(ChannelHandlerContext ctx) {
    String sourceKey = sourceKey(ctx.channel());

    LOGGER.debug("client disconnected: {}", sourceKey);

    serverContext.removeSession(sourceKey);
  }

  @Override
  public void receive(ChannelHandlerContext ctx, RedisToken message) {
    String sourceKey = sourceKey(ctx.channel());

    LOGGER.debug("message received: {}", sourceKey);

    parseMessage(message, getSession(ctx, sourceKey))
      .ifPresent(serverContext::processCommand);
  }

  private Optional<Request> parseMessage(RedisToken message, Session session) {
    if (message instanceof ArrayRedisToken) {
      return Optional.of(parseArray((ArrayRedisToken) message, session));
    } else if (message instanceof UnknownRedisToken) {
      return Optional.of(parseLine((UnknownRedisToken) message, session));
    }
    return Optional.empty();
  }

  private Request parseLine(UnknownRedisToken message, Session session) {
    SafeString command = message.getValue();
    String[] params = command.toString().split(" ");
    String[] array = new String[params.length - 1];
    System.arraycopy(params, 1, array, 0, array.length);
    return new DefaultRequest(serverContext, session, safeString(params[0]), safeAsList(array));
  }

  private Request parseArray(ArrayRedisToken message, Session session) {
    List<SafeString> params = toParams(message);
    return new DefaultRequest(serverContext, session, params.remove(0), params);
  }

  private List<SafeString> toParams(ArrayRedisToken message) {
    List<SafeString> result = new ArrayList<>();
    for (RedisToken token : message.getValue()) {
      result.addAll(toSafeStrings(token));
    }
    return result;
  }

  private List<SafeString> toSafeStrings(RedisToken token) {
    if (token instanceof StringRedisToken) {
      return Collections.singletonList(((StringRedisToken) token).getValue());
    }
    return Collections.emptyList();
  }

  private String sourceKey(Channel channel) {
    InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
    return remoteAddress.getHostName() + ":" + remoteAddress.getPort();
  }

  private Session getSession(ChannelHandlerContext ctx, String sourceKey) {
    return serverContext.getSession(sourceKey, key -> newSession(ctx, key));
  }

  private Session newSession(ChannelHandlerContext ctx, String key) {
    return new DefaultSession(key, ctx);
  }

  private static EventLoopGroup closeWorker(EventLoopGroup worker) {
    if (worker != null) {
      closeFuture(worker.shutdownGracefully());
    }
    return null;
  }

  private static void closeFuture(Future<?> future) {
    LOGGER.debug("closing future");
    future.syncUninterruptibly();
    LOGGER.debug("future closed");
  }

  public static class Builder implements Recoverable {

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private CommandSuite commands = new CommandSuite();

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder randomPort() {
      try (ServerSocket socket = new ServerSocket(0)) {
        socket.setReuseAddress(true);
        this.port = socket.getLocalPort();
      } catch (IOException e) {
        return sneakyThrow(e);
      }
      return this;
    }

    public Builder commands(CommandSuite commands) {
      this.commands = commands;
      return this;
    }

    public RespServer build() {
      return new RespServer(new RespServerContext(host, port, commands));
    }
  }
}
