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
import java.util.Objects;

public abstract class RedisToken<T> {

  private static final StringRedisToken NULL_STRING = new StringRedisToken(null);
  private static final StatusRedisToken RESPONSE_OK = new StatusRedisToken(safeString("OK"));

  private static final String SEPARATOR = "=>";

  private final RedisTokenType type;

  private final T value;

  private RedisToken(RedisTokenType type, T value) {
    this.type = requireNonNull(type);
    this.value = value;
  }

  public RedisTokenType getType() {
    return type;
  }

  public T getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public boolean equals(Object obj) {
    return equalizer(this)
        .append((one, other) -> Objects.equals(one.value, other.value))
        .applyTo(obj);
  }

  @Override
  public String toString() {
    return type + SEPARATOR + value;
  }

  public static StringRedisToken nullString() {
    return NULL_STRING;
  }

  public static StatusRedisToken responseOk() {
    return RESPONSE_OK;
  }

  public static StringRedisToken string(SafeString str) {
    return new StringRedisToken(str);
  }

  public static StringRedisToken string(String str) {
    return new StringRedisToken(safeString(str));
  }

  public static StatusRedisToken status(String str) {
    return new StatusRedisToken(safeString(str));
  }

  public static IntegerRedisToken integer(boolean b) {
    return new IntegerRedisToken(b ? 1 : 0);
  }

  public static IntegerRedisToken integer(int i) {
    return new IntegerRedisToken(i);
  }

  public static ErrorRedisToken error(String str) {
    return new ErrorRedisToken(safeString(str));
  }

  public static ArrayRedisToken array(RedisToken<?>... redisTokens) {
    return new ArrayRedisToken(asList(redisTokens));
  }

  public static ArrayRedisToken array(Collection<RedisToken<?>> redisTokens) {
    return new ArrayRedisToken(redisTokens);
  }

  public static final class UnknownRedisToken extends RedisToken<SafeString> {
    UnknownRedisToken(SafeString value) {
      super(RedisTokenType.UNKNOWN, value);
    }
  }

  public static final class StringRedisToken extends RedisToken<SafeString> {
    StringRedisToken(SafeString value) {
      super(RedisTokenType.STRING, value);
    }
  }

  public static final class StatusRedisToken extends RedisToken<SafeString> {
    StatusRedisToken(SafeString value) {
      super(RedisTokenType.STATUS, value);
    }
  }

  public static final class ErrorRedisToken extends RedisToken<SafeString> {
    ErrorRedisToken(SafeString value) {
      super(RedisTokenType.ERROR, value);
    }
  }

  public static final class IntegerRedisToken extends RedisToken<Integer> {
    IntegerRedisToken(Integer value) {
      super(RedisTokenType.INTEGER, value);
    }
  }

  public static final class ArrayRedisToken extends RedisToken<Collection<RedisToken<?>>> {
    ArrayRedisToken(Collection<RedisToken<?>> value) {
      super(RedisTokenType.ARRAY, unmodifiableList(new ArrayList<>(value)));
    }

    public int size() {
      return getValue().size();
    }
  }
}