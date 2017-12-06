/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static java.util.stream.Collectors.toList;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tonivade.resp.command.DefaultRequest;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.command.RespCommand;
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
import io.reactivex.Observable;

public class RespServer implements Resp {

  private static final Logger LOGGER = LoggerFactory.getLogger(RespServer.class);

  private static final int BUFFER_SIZE = 1024 * 1024;
  private static final int MAX_FRAME_SIZE = BUFFER_SIZE * 100;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private ServerBootstrap bootstrap;
  private RespInitializerHandler acceptHandler;
  private RespConnectionHandler connectionHandler;
  private ChannelFuture future;
  
  private final RespServerContext serverContext;

  public RespServer(RespServerContext serverContext) {
    this.serverContext = serverContext;
  }

  public void start() {
    bossGroup = new NioEventLoopGroup();
    workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    acceptHandler = new RespInitializerHandler(this);
    connectionHandler = new RespConnectionHandler(this);

    bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(acceptHandler)
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
        LOGGER.debug("closing future");
        future.channel().close().syncUninterruptibly();
        LOGGER.debug("future closed");
        future = null;
      }
    } finally {
      if (workerGroup != null) {
        LOGGER.debug("workerGroup future");
        workerGroup.shutdownGracefully().syncUninterruptibly();
        LOGGER.debug("workerGroup closed");
        workerGroup = null;
      }
      if (bossGroup != null) {
        LOGGER.debug("bossgroup future");
        bossGroup.shutdownGracefully().syncUninterruptibly();
        LOGGER.debug("bossGroup closed");
        bossGroup = null;
      }
    }

    serverContext.stop();

    LOGGER.info("server stopped");
  }

  @Override
  public void channel(SocketChannel channel) {
    LOGGER.debug("new channel: {}", sourceKey(channel));

    channel.pipeline().addLast("redisEncoder", new RedisEncoder());
    channel.pipeline().addLast("linDelimiter", new RedisDecoder(MAX_FRAME_SIZE));
    channel.pipeline().addLast(connectionHandler);
  }

  @Override
  public void connected(ChannelHandlerContext ctx) {
    String sourceKey = sourceKey(ctx.channel());
    LOGGER.debug("client connected: {}", sourceKey);
    serverContext.getSession(sourceKey, ctx);
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

    parseMessage(message, serverContext.getSession(sourceKey, ctx)).ifPresent(this::processCommand);
  }

  private Optional<Request> parseMessage(RedisToken message, Session session) {
    return Match(message)
        .of(Case($(instanceOf(ArrayRedisToken.class)), token -> Optional.of(parseArray(token, session))),
            Case($(instanceOf(UnknownRedisToken.class)), token -> Optional.of(parseLine(token, session))),
            Case($(), token -> Optional.empty()));
  }

  private Request parseLine(UnknownRedisToken message, Session session) {
    SafeString command = message.getValue();
    String[] params = command.toString().split(" ");
    String[] array = new String[params.length - 1];
    System.arraycopy(params, 1, array, 0, array.length);
    return new DefaultRequest(serverContext, session, safeString(params[0]), safeAsList(array));
  }

  private Request parseArray(ArrayRedisToken message, Session session) {
    List<SafeString> params = message.getValue().stream()
        .flatMap(token -> Match(token)
                 .of(Case($(instanceOf(StringRedisToken.class)), string -> Stream.of(string.getValue())),
                     Case($(), Stream.empty())))
        .collect(toList());
    return new DefaultRequest(serverContext, session, params.remove(0), params);
  }

  private void processCommand(Request request) {
    LOGGER.debug("received command: {}", request);

    Session session = request.getSession();
    RespCommand command = serverContext.getCommand(request.getCommand());
    try {
      serverContext.executeOn(execute(command, request)).subscribe(token -> {
        session.publish(token);
        if (request.isExit()) {
          session.close();
        }
      });
    } catch (RuntimeException e) {
      LOGGER.error("error executing command: " + request, e);
    }
  }

  private Observable<RedisToken> execute(RespCommand command, Request request) {
    return Observable.create(observer -> {
      observer.onNext(serverContext.executeCommand(command, request));
      observer.onComplete();
    });
  }

  private String sourceKey(Channel channel) {
    InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
    return remoteAddress.getHostName() + ":" + remoteAddress.getPort();
  }
}
