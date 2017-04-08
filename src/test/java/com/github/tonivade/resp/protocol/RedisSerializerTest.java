/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;

import org.junit.Test;

public class RedisSerializerTest {

  private Charset utf8 = Charset.forName("UTF-8");

  private RedisSerializer encoder = new RedisSerializer();

  private RedisToken<?> intToken = RedisToken.integer(1);
  private RedisToken<?> abcString = RedisToken.string("abc");
  private RedisToken<?> pongString = RedisToken.status("pong");
  private RedisToken<?> errorString = RedisToken.error("ERR");
  private RedisToken<?> arrayToken = RedisToken.array(intToken, abcString);
  private RedisToken<?> arrayOfArraysToken = RedisToken.array(arrayToken, arrayToken);

  @Test
  public void encodeString() {
    assertThat(encoder.encodeToken(abcString), equalTo("$3\r\nabc\r\n".getBytes(utf8)));
  }

  @Test
  public void encodeStatus() {
    assertThat(encoder.encodeToken(pongString), equalTo("+pong\r\n".getBytes(utf8)));
  }

  @Test
  public void encodeInteger() {
    assertThat(encoder.encodeToken(intToken), equalTo(":1\r\n".getBytes(utf8)));
  }

  @Test
  public void encodeError() {
    assertThat(encoder.encodeToken(errorString), equalTo("-ERR\r\n".getBytes(utf8)));
  }

  @Test
  public void encodeArray() {
    assertThat(encoder.encodeToken(arrayToken), equalTo("*2\r\n:1\r\n$3\r\nabc\r\n".getBytes(utf8)));
  }

  @Test
  public void encodeArrayOfArrays() {
    assertThat(encoder.encodeToken(arrayOfArraysToken),
        equalTo("*2\r\n*2\r\n:1\r\n$3\r\nabc\r\n*2\r\n:1\r\n$3\r\nabc\r\n".getBytes(utf8)));
  }
}
