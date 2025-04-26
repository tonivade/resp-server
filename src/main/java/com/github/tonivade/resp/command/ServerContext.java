/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import java.util.Optional;

public interface ServerContext {
  String getHost();
  int getPort();
  int getClients();
  RespCommand getCommand(String name);
  <T> Optional<T> getValue(String key);
  void putValue(String key, Object value);
  <T> Optional<T> removeValue(String key);
}
