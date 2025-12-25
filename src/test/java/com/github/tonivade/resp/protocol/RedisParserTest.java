/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;
import com.github.tonivade.resp.util.StringRedisSource;
import org.junit.jupiter.api.Test;

import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class RedisParserTest {

  private StringRedisSource source = new StringRedisSource();

  private RedisParser parser = new RedisParser(100000, source);

  private RedisToken intToken = RedisToken.integer(1);
  private RedisToken abcString = RedisToken.string("abc");
  private RedisToken emptyString = RedisToken.string("");
  private RedisToken pongString = RedisToken.status("pong");
  private RedisToken errorString = RedisToken.error("ERR");
  private RedisToken arrayToken = RedisToken.array(intToken, abcString);
  private RedisToken unknownString = new UnknownRedisToken(safeString("what?"));

  @Test
  void testBulkString() {
    source.init("$3\r\nabc\r\n");

    RedisToken token = parser.next();

    assertThat(token, equalTo(abcString));
    assertThat(source.available(), equalTo(0));
  }

  @Test
  void testEmptyBulkString() {
    source.init("$0\r\n\r\n");

    RedisToken token = parser.next();

    assertThat(token, equalTo(emptyString));
    assertThat(source.available(), equalTo(0));
  }

  @Test
  void testSimpleString() {
    source.init("+pong\r\n");

    RedisToken token = parser.next();

    assertThat(token, equalTo(pongString));
    assertThat(source.available(), equalTo(0));
  }

  @Test
  void testInteger() {
    source.init(":1\r\n");

    RedisToken token = parser.next();

    assertThat(token, equalTo(intToken));
    assertThat(source.available(), equalTo(0));
  }

  @Test
  void testErrorString() {
    source.init("-ERR\r\n");

    RedisToken token = parser.next();

    assertThat(token, equalTo(errorString));
    assertThat(source.available(), equalTo(0));
  }

  @Test
  void testUnknownToken() {
    source.init("what?\r\n");

    RedisToken token = parser.next();

    assertThat(token, equalTo(unknownString));
    assertThat(source.available(), equalTo(0));
  }

  @Test
  void testArray() {
    source.init(
      "*2\r\n",
      ":1\r\n",
      "$3\r\n",
      "abc\r\n"
    );

    RedisToken token = parser.next();

    assertThat(token, equalTo(arrayToken));
    assertThat(source.available(), equalTo(0));
  }
}
