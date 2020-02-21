/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import java.util.HashMap;
import java.util.Map;

import com.github.tonivade.purefun.type.Option;

public class StateHolder {
  private final Map<String, Object> state = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> Option<T> getValue(String key) {
    return (Option<T>) Option.of(() -> state.get(key));
  }

  @SuppressWarnings("unchecked")
  public <T> Option<T> removeValue(String key) {
    return (Option<T>) Option.of(() -> state.remove(key));
  }

  public void putValue(String key, Object value) {
    state.put(key, value);
  }

  public void clear() {
    state.clear();
  }
}
