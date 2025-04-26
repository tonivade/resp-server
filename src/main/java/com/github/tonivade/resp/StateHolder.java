/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StateHolder {

  private final Map<String, Object> state = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> Optional<T> getValue(String key) {
    return (Optional<T>) Optional.ofNullable(state.get(key));
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> removeValue(String key) {
    return (Optional<T>) Optional.ofNullable(state.remove(key));
  }

  public void putValue(String key, Object value) {
    state.put(key, value);
  }

  public void clear() {
    state.clear();
  }
}
