/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

public enum RedisTokenType {
  STATUS,
  INTEGER,
  STRING,
  ARRAY,
  ERROR,
  UNKNOWN
}
