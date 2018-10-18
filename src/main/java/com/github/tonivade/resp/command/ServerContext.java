/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import com.github.tonivade.purefun.type.Option;

public interface ServerContext {
  String getHost();
  int getPort();
  int getClients();
  RespCommand getCommand(String name);
  <T> Option<T> getValue(String key);
  void putValue(String key, Object value);
  <T> Option<T> removeValue(String key);
}
