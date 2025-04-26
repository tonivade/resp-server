/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.util;

public interface Recoverable {

  // XXX: https://www.baeldung.com/java-sneaky-throws
  @SuppressWarnings("unchecked")
  default <X extends Throwable, R> R sneakyThrow(Throwable t) throws X {
    throw (X) t;
  }
}
