/*
 * Copyright (c) 2015-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.github.tonivade.resp.protocol.RedisSerializer.encodeToken;

import org.junit.jupiter.api.Test;

class RedisSerializerTest {

  private RedisToken intToken = RedisToken.integer(1);
  private RedisToken abcString = RedisToken.string("abc");
  private RedisToken pongString = RedisToken.status("pong");
  private RedisToken errorString = RedisToken.error("ERR");
  private RedisToken arrayToken = RedisToken.array(intToken, abcString);
  private RedisToken arrayOfArraysToken = RedisToken.array(arrayToken, arrayToken);

  @Test
  void encodeString() {
    assertThat(encodeToken(abcString), equalTo("$3\r\nabc\r\n".getBytes(UTF_8)));
  }

  @Test
  void encodeStatus() {
    assertThat(encodeToken(pongString), equalTo("+pong\r\n".getBytes(UTF_8)));
  }

  @Test
  void encodeInteger() {
    assertThat(encodeToken(intToken), equalTo(":1\r\n".getBytes(UTF_8)));
  }

  @Test
  void encodeError() {
    assertThat(encodeToken(errorString), equalTo("-ERR\r\n".getBytes(UTF_8)));
  }

  @Test
  void encodeArray() {
    assertThat(encodeToken(arrayToken), equalTo("*2\r\n:1\r\n$3\r\nabc\r\n".getBytes(UTF_8)));
  }

  @Test
  void encodeArrayOfArrays() {
    assertThat(encodeToken(arrayOfArraysToken),
        equalTo("*2\r\n*2\r\n:1\r\n$3\r\nabc\r\n*2\r\n:1\r\n$3\r\nabc\r\n".getBytes(UTF_8)));
  }
}
