/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.command.DefaultSession;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.command.RespCommand;
import com.github.tonivade.resp.command.ServerContext;
import com.github.tonivade.resp.command.Session;
import com.github.tonivade.resp.protocol.RedisToken;

import io.netty.channel.ChannelHandlerContext;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class RespServerContext implements ServerContext {
  private final Map<String, Object> state = new HashMap<>();
  private final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
  private final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

  private final String host;
  private final int port;
  private final CommandSuite commands;
  
  public RespServerContext(String host, int port, CommandSuite commands) {
    this.host = requireNonNull(host);
    this.port = requireRange(port, 1024, 65535);
    this.commands = requireNonNull(commands);
  }
  
  public void start() {
    
  }
  
  public void stop() {
    
  }

  @Override
  public int getClients() {
    return clients.size();
  }

  @Override
  public RespCommand getCommand(String name) {
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
  
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }

  void clear() {
    clients.clear();
    state.clear();
  }

  Session getSession(String sourceKey, ChannelHandlerContext ctx) {
    return clients.computeIfAbsent(sourceKey, key -> newSession(ctx, key));
  }

  protected CommandSuite getCommands() {
    return commands;
  }

  protected void removeSession(String sourceKey) {
    Session session = clients.remove(sourceKey);
    if (session != null) {
      cleanSession(session);
    }
  }

  protected Session getSession(String key) {
    return clients.get(key);
  }

  protected RedisToken executeCommand(RespCommand command, Request request) {
    return command.execute(request);
  }

  protected <T> Observable<T> executeOn(Observable<T> observable) {
    return observable.observeOn(scheduler);
  }

  protected void cleanSession(Session session) {

  }

  protected void createSession(Session session) {

  }

  private Session newSession(ChannelHandlerContext ctx, String key) {
    DefaultSession session = new DefaultSession(key, ctx);
    createSession(session);
    return session;
  }

  private int requireRange(int value, int min, int max) {
    if (value <= min || value > max) {
      throw new IllegalArgumentException(min + " <= " + value + " < " + max);
    }
    return value;
  }
}
