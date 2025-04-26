/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

public interface RedisSource {

  int available();
  SafeString readLine();
  SafeString readString(int length);
}
