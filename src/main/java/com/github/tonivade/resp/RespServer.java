/*
 * Copyright (c) 2015-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static java.util.stream.Collectors.toList;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Pattern1;
import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.type.Option;
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
        .option(ChannelOption.SO_RCVBUF, BUFFER_SIZE)
        .option(ChannelOption.SO_SNDBUF, BUFFER_SIZE)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

    future = bootstrap.bind(serverContext.getHost(), serverContext.getPort());
    // Bind and start to accept incoming connections.
    future.syncUninterruptibly();

    serverContext.start();

    LOGGER.info("server started: {}:{}", serverContext.getHost(), serverContext.getPort());
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
    LOGGER.debug("new channel: {}", sourceKey(channel));

    channel.pipeline().addLast("redisEncoder", new RedisEncoder());
    channel.pipeline().addLast("linDelimiter", new RedisDecoder(MAX_FRAME_SIZE));
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

  private Option<Request> parseMessage(RedisToken message, Session session) {
    return Pattern1.<RedisToken, Option<Request>>build()
        .when(Matcher1.instanceOf(ArrayRedisToken.class))
          .then(token -> Option.some(parseArray((ArrayRedisToken) token, session)))
        .when(Matcher1.instanceOf(UnknownRedisToken.class))
          .then(token -> Option.some(parseLine((UnknownRedisToken) token, session)))
        .otherwise()
          .returns(Option.none())
        .apply(message);
  }

  private Request parseLine(UnknownRedisToken message, Session session) {
    SafeString command = message.getValue();
    String[] params = command.toString().split(" ");
    String[] array = new String[params.length - 1];
    System.arraycopy(params, 1, array, 0, array.length);
    return new DefaultRequest(serverContext, session, safeString(params[0]), ImmutableArray.from(safeAsList(array)));
  }

  private Request parseArray(ArrayRedisToken message, Session session) {
    List<SafeString> params = toParams(message);
    return new DefaultRequest(serverContext, session, params.remove(0), ImmutableArray.from(params));
  }

  private List<SafeString> toParams(ArrayRedisToken message) {
    return message.getValue().stream()
        .flatMap(this::toSafeStrings)
        .collect(toList());
  }

  private Stream<SafeString> toSafeStrings(RedisToken token) {
    return Pattern1.<RedisToken, Stream<SafeString>>build()
        .when(Matcher1.instanceOf(StringRedisToken.class))
          .then(string -> Stream.of(((StringRedisToken) string).getValue()))
        .otherwise()
          .returns(Stream.empty())
        .apply(token);
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

  public static class Builder {
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

    public Builder commands(CommandSuite commands) {
      this.commands = commands;
      return this;
    }

    public RespServer build() {
      return new RespServer(new RespServerContext(host, port, commands));
    }
  }
}
