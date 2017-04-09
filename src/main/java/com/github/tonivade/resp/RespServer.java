/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.command.ICommand;
import com.github.tonivade.resp.command.IRequest;
import com.github.tonivade.resp.command.IServerContext;
import com.github.tonivade.resp.command.ISession;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.command.Session;
import com.github.tonivade.resp.protocol.RedisDecoder;
import com.github.tonivade.resp.protocol.RedisEncoder;
import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.RedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.RedisToken.StringRedisToken;
import com.github.tonivade.resp.protocol.RedisTokenType;
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
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class RespServer implements Resp, IServerContext {

  private static final Logger LOGGER = Logger.getLogger(RespServer.class.getName());

  private static final int BUFFER_SIZE = 1024 * 1024;
  private static final int MAX_FRAME_SIZE = BUFFER_SIZE * 100;

  private final int port;
  private final String host;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  private ServerBootstrap bootstrap;

  private RespInitializerHandler acceptHandler;
  private RespConnectionHandler connectionHandler;

  private ChannelFuture future;

  private final Map<String, Object> state = new HashMap<>();

  private final ConcurrentHashMap<String, ISession> clients = new ConcurrentHashMap<>();

  private final CommandSuite commands;

  private final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

  public RespServer(String host, int port, CommandSuite commands) {
    this.host = requireNonNull(host);
    this.port = requireRange(port, 1024, 65535);
    this.commands = requireNonNull(commands);
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

    future = bootstrap.bind(host, port);
    // Bind and start to accept incoming connections.
    future.syncUninterruptibly();

    LOGGER.info(() -> "server started: " + host + ":" + port);
  }

  public void stop() {
    try {
      if (future != null) {
        future.channel().close();
      }
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }

    clients.clear();

    LOGGER.info(() -> "server stopped");
  }

  @Override
  public void channel(SocketChannel channel) {
    LOGGER.fine(() -> "new channel: " + sourceKey(channel));

    channel.pipeline().addLast("redisEncoder", new RedisEncoder());
    channel.pipeline().addLast("linDelimiter", new RedisDecoder(MAX_FRAME_SIZE));
    channel.pipeline().addLast(connectionHandler);
  }

  @Override
  public void connected(ChannelHandlerContext ctx) {
    String sourceKey = sourceKey(ctx.channel());
    LOGGER.fine(() -> "client connected: " + sourceKey);
    getSession(sourceKey, ctx);
  }

  @Override
  public void disconnected(ChannelHandlerContext ctx) {
    String sourceKey = sourceKey(ctx.channel());

    LOGGER.fine(() -> "client disconnected: " + sourceKey);

    ISession session = clients.remove(sourceKey);
    if (session != null) {
      cleanSession(session);
    }
  }

  @Override
  public void receive(ChannelHandlerContext ctx, RedisToken<?> message) {
    String sourceKey = sourceKey(ctx.channel());

    LOGGER.finest(() -> "message received: " + sourceKey);

    parseMessage(sourceKey, message, getSession(sourceKey, ctx)).ifPresent(this::processCommand);
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public int getClients() {
    return clients.size();
  }

  @Override
  public ICommand getCommand(String name) {
    return commands.getCommand(name);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Optional<T> getValue(String key) {
    return (Optional<T>) Optional.ofNullable(state.get(key));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Optional<T> removeValue(String key) {
    return (Optional<T>) Optional.ofNullable(state.remove(key));
  }

  @Override
  public void putValue(String key, Object value) {
    state.put(key, value);
  }

  public ISession getSession(String key) {
    return clients.get(key);
  }

  public CommandSuite getCommands() {
    return commands;
  }

  protected RedisToken<?> executeCommand(ICommand command, IRequest request) {
    return command.execute(request);
  }

  protected void cleanSession(ISession session) {

  }

  protected void createSession(ISession session) {

  }

  private ISession getSession(String sourceKey, ChannelHandlerContext ctx) {
    return clients.computeIfAbsent(sourceKey, key -> newSession(ctx, key));
  }

  private ISession newSession(ChannelHandlerContext ctx, String key) {
    Session session = new Session(key, ctx);
    createSession(session);
    return session;
  }

  private Optional<IRequest> parseMessage(String sourceKey, RedisToken<?> message, ISession session) {
    IRequest request = null;
    if (message.getType() == RedisTokenType.ARRAY) {
      request = parseArray(sourceKey, (ArrayRedisToken) message, session);
    } else if (message.getType() == RedisTokenType.UNKNOWN) {
      request = parseLine(sourceKey, (StringRedisToken) message, session);
    }
    return Optional.ofNullable(request);
  }

  private Request parseLine(String sourceKey, StringRedisToken message, ISession session) {
    SafeString command = message.getValue();
    String[] params = command.toString().split(" ");
    String[] array = new String[params.length - 1];
    System.arraycopy(params, 1, array, 0, array.length);
    return new Request(this, session, safeString(params[0]), safeAsList(array));
  }

  private Request parseArray(String sourceKey, ArrayRedisToken message, ISession session) {
    List<SafeString> params = new LinkedList<>();
    for (RedisToken<?> token : message.getValue()) {
      if (token.getType() == RedisTokenType.STRING) {
        params.add((SafeString) token.getValue());
      }
    }
    return new Request(this, session, params.remove(0), params);
  }

  private void processCommand(IRequest request) {
    LOGGER.fine(() -> "received command: " + request);

    ISession session = request.getSession();
    ICommand command = commands.getCommand(request.getCommand());
    try {
      execute(command, request).observeOn(scheduler).subscribe(token -> {
        session.publish(token);
        if (request.isExit()) {
          session.close();
        }
      });
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "error executing command: " + request, e);
    }
  }

  private Observable<RedisToken<?>> execute(ICommand command, IRequest request) {
    return Observable.create(observer -> {
      observer.onNext(executeCommand(command, request));
      observer.onComplete();
    });
  }

  private String sourceKey(Channel channel) {
    InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
    return remoteAddress.getHostName() + ":" + remoteAddress.getPort();
  }

  private int requireRange(int value, int min, int max) {
    if (value <= min || value > max) {
      throw new IllegalArgumentException(min + " <= " + value + " < " + max);
    }
    return value;
  }
}
