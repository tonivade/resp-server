/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.purefun.Precondition.checkNonEmpty;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.Precondition.checkRange;
import static com.github.tonivade.resp.SessionListener.nullListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.command.RespCommand;
import com.github.tonivade.resp.command.ServerContext;
import com.github.tonivade.resp.command.Session;
import com.github.tonivade.resp.protocol.RedisToken;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RespServerContext implements ServerContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(RespServerContext.class);

  private final StateHolder state = new StateHolder();
  private final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
  private final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

  private final String host;
  private final int port;
  private final CommandSuite commands;
  private SessionListener sessionListener;

  public RespServerContext(String host, int port, CommandSuite commands) {
    this(host, port, commands, nullListener());
  }

  public RespServerContext(String host, int port, CommandSuite commands,
                           SessionListener sessionListener) {
    this.host = checkNonEmpty(host);
    this.port = checkRange(port, 1024, 65535);
    this.commands = checkNonNull(commands);
    this.sessionListener = sessionListener;
  }

  public void start() {

  }

  public void stop() {
    clear();
    scheduler.shutdown();
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
  public <T> Option<T> getValue(String key) {
    return state.getValue(key);
  }

  @Override
  public <T> Option<T> removeValue(String key) {
    return state.removeValue(key);
  }

  @Override
  public void putValue(String key, Object value) {
    state.putValue(key, value);
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }

  Session getSession(String sourceKey, Function1<String, Session> factory) {
    return clients.computeIfAbsent(sourceKey, key -> {
      Session session = factory.apply(key);
      sessionListener.sessionCreated(session);
      return session;
    });
  }

  void processCommand(Request request) {
    LOGGER.debug("received command: {}", request);

    RespCommand command = getCommand(request.getCommand());
    try {
      executeOn(execute(command, request))
        .subscribe(response -> processResponse(request, response),
                   ex -> LOGGER.error("error executing command: " + request, ex));
    } catch (RuntimeException ex) {
      LOGGER.error("error executing command: " + request, ex);
    }
  }

  protected CommandSuite getCommands() {
    return commands;
  }

  protected void removeSession(String sourceKey) {
    Session session = clients.remove(sourceKey);
    if (session != null) {
      sessionListener.sessionDeleted(session);
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

  private void processResponse(Request request, RedisToken token) {
    request.getSession().publish(token);
    if (request.isExit()) {
      request.getSession().close();
    }
  }

  private Observable<RedisToken> execute(RespCommand command, Request request) {
    return Observable.create(observer -> {
      observer.onNext(executeCommand(command, request));
      observer.onComplete();
    });
  }

  private void clear() {
    clients.clear();
    state.clear();
  }
}
