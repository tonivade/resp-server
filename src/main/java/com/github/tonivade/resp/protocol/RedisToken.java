/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static tonivade.equalizer.Equalizer.equalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class RedisToken {

  private static final RedisToken NULL_STRING = new StringRedisToken(null);
  private static final RedisToken RESPONSE_OK = new StatusRedisToken(safeString("OK"));

  private static final String SEPARATOR = "=>";

  private final RedisTokenType type;

  private final Object value;

  private RedisToken(RedisTokenType type, Object value) {
    this.type = requireNonNull(type);
    this.value = value;
  }

  public RedisTokenType getType() {
    return type;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T) value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public boolean equals(Object obj) {
    return equalizer(this).append((one, other) -> Objects.equals(one.value, other.value)).applyTo(obj);
  }

  @Override
  public String toString() {
    return type + SEPARATOR + value;
  }

  public static RedisToken nullString() {
    return NULL_STRING;
  }

  public static RedisToken responseOk() {
    return RESPONSE_OK;
  }

  public static RedisToken string(SafeString str) {
    return new StringRedisToken(str);
  }

  public static RedisToken string(String str) {
    return new StringRedisToken(safeString(str));
  }

  public static RedisToken status(String str) {
    return new StatusRedisToken(safeString(str));
  }

  public static RedisToken integer(boolean b) {
    return new IntegerRedisToken(b ? 1 : 0);
  }

  public static RedisToken integer(int i) {
    return new IntegerRedisToken(i);
  }

  public static RedisToken error(String str) {
    return new ErrorRedisToken(safeString(str));
  }

  public static RedisToken array(RedisToken... redisTokens) {
    return new ArrayRedisToken(asList(redisTokens));
  }

  public static RedisToken array(Collection<RedisToken> redisTokens) {
    return new ArrayRedisToken(redisTokens);
  }

  static class UnknownRedisToken extends RedisToken {
    public UnknownRedisToken(SafeString value) {
      super(RedisTokenType.UNKNOWN, value);
    }
  }

  static class StringRedisToken extends RedisToken {
    public StringRedisToken(SafeString value) {
      super(RedisTokenType.STRING, value);
    }
  }

  static class StatusRedisToken extends RedisToken {
    public StatusRedisToken(SafeString value) {
      super(RedisTokenType.STATUS, value);
    }
  }

  static class ErrorRedisToken extends RedisToken {
    public ErrorRedisToken(SafeString value) {
      super(RedisTokenType.ERROR, value);
    }
  }

  static class IntegerRedisToken extends RedisToken {
    public IntegerRedisToken(Integer value) {
      super(RedisTokenType.INTEGER, value);
    }
  }

  static class ArrayRedisToken extends RedisToken {
    public ArrayRedisToken(Collection<RedisToken> value) {
      super(RedisTokenType.ARRAY, unmodifiableList(new ArrayList<>(value)));
    }

    public int size() {
      return this.<List<RedisToken>>getValue().size();
    }
  }
}