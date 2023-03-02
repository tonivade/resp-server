/*
 * Copyright (c) 2015-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;

class RedisParserTest {

  private RedisSource source = Mockito.mock(RedisSource.class);

  private RedisParser parser = new RedisParser(100000, source);

  private RedisToken intToken = RedisToken.integer(1);
  private RedisToken abcString = RedisToken.string("abc");
  private RedisToken pongString = RedisToken.status("pong");
  private RedisToken errorString = RedisToken.error("ERR");
  private RedisToken arrayToken = RedisToken.array(intToken, abcString);
  private RedisToken unknownString = new UnknownRedisToken(safeString("what?"));

  @Test
  void testBulkString() {
    when(source.readLine()).thenReturn(safeString("$3"));
    when(source.readString(3)).thenReturn(safeString("abc"));

    RedisToken token = parser.next();

    assertThat(token, equalTo(abcString));
  }

  @Test
  void testSimpleString() {
    when(source.readLine()).thenReturn(safeString("+pong"));

    RedisToken token = parser.next();

    assertThat(token, equalTo(pongString));
  }

  @Test
  void testInteger() {
    when(source.readLine()).thenReturn(safeString(":1"));

    RedisToken token = parser.next();

    assertThat(token, equalTo(intToken));
  }

  @Test
  void testErrorString() {
    when(source.readLine()).thenReturn(safeString("-ERR"));

    RedisToken token = parser.next();

    assertThat(token, equalTo(errorString));
  }

  @Test
  void testUnknownString() {
    when(source.readLine()).thenReturn(safeString("what?"));

    RedisToken token = parser.next();

    assertThat(token, equalTo(unknownString));
  }

  @Test
  void testArray() {
    when(source.readLine()).thenReturn(safeString("*2"), safeString(":1"), safeString("$3"));
    when(source.readString(3)).thenReturn(safeString("abc"));

    RedisToken token = parser.next();

    assertThat(token, equalTo(arrayToken));
  }
}
