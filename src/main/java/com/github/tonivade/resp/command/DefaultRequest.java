/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.resp.protocol.SafeString;

public class DefaultRequest implements Request {

  private final SafeString command;

  private final ImmutableArray<SafeString> params;

  private final Session session;

  private final ServerContext server;

  public DefaultRequest(ServerContext server, Session session, SafeString command, ImmutableArray<SafeString> params) {
    this.server = server;
    this.session = session;
    this.command = requireNonNull(command);
    this.params = requireNonNull(params);
  }

  @Override
  public String getCommand() {
    return command.toString();
  }

  @Override
  public ImmutableArray<SafeString> getParams() {
    return params;
  }

  @Override
  public SafeString getParam(int i) {
    if (i < params.size()) {
      return params.get(i);
    }
    return null;
  }

  @Override
  public Option<SafeString> getOptionalParam(int i) {
    return Option.of(() -> getParam(i));
  }

  @Override
  public int getLength() {
    return params.size();
  }

  @Override
  public boolean isEmpty() {
    return params.isEmpty();
  }

  @Override
  public boolean isExit() {
    return command.toString().equalsIgnoreCase("quit");
  }

  @Override
  public Session getSession() {
    return session;
  }

  @Override
  public ServerContext getServerContext() {
    return server;
  }

  @Override
  public String toString() {
    return command + "[" + params.size() + "]: " + params;
  }
}
