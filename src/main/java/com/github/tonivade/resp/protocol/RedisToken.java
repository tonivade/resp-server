/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.SafeString.safeString;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ErrorRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StatusRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;

public interface RedisToken {
  RedisToken NULL_STRING = string((SafeString) null);
  RedisToken RESPONSE_OK = status("OK");

  RedisTokenType getType();

  <T> T accept(RedisTokenVisitor<T> visitor);

  static RedisToken nullString() {
    return NULL_STRING;
  }

  static RedisToken responseOk() {
    return RESPONSE_OK;
  }

  static RedisToken string(SafeString str) {
    return new StringRedisToken(str);
  }

  static RedisToken string(String str) {
    return new StringRedisToken(safeString(str));
  }

  static RedisToken status(String str) {
    return new StatusRedisToken(str);
  }

  static RedisToken integer(boolean b) {
    return new IntegerRedisToken(b ? 1 : 0);
  }

  static RedisToken integer(int i) {
    return new IntegerRedisToken(i);
  }

  static RedisToken error(String str) {
    return new ErrorRedisToken(str);
  }

  static RedisToken array(RedisToken... redisTokens) {
    return new ArrayRedisToken(Arrays.asList(redisTokens));
  }

  static RedisToken array(Collection<RedisToken> redisTokens) {
    return new ArrayRedisToken(redisTokens);
  }

  static <T> Stream<T> visit(Stream<RedisToken> tokens, RedisTokenVisitor<T> visitor) {
    return tokens.map(token -> token.accept(visitor));
  }
}
