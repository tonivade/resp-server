/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static tonivade.equalizer.Equalizer.equalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class AbstractRedisToken<T> implements RedisToken {

  static final StringRedisToken NULL_STRING = new StringRedisToken(null);
  static final StatusRedisToken RESPONSE_OK = new StatusRedisToken(safeString("OK"));

  private static final String SEPARATOR = "=>";

  private final RedisTokenType type;

  private final T value;

  private AbstractRedisToken(RedisTokenType type, T value) {
    this.type = requireNonNull(type);
    this.value = value;
  }

  @Override
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

  public static final class UnknownRedisToken extends AbstractRedisToken<SafeString> {
    UnknownRedisToken(SafeString value) {
      super(RedisTokenType.UNKNOWN, value);
    }
    
    @Override
    public void accept(RedisTokenVisitor visitor)
    {
      visitor.unknown(this);
    }
  }

  public static final class StringRedisToken extends AbstractRedisToken<SafeString> {
    StringRedisToken(SafeString value) {
      super(RedisTokenType.STRING, value);
    }
    
    @Override
    public void accept(RedisTokenVisitor visitor)
    {
      visitor.string(this);
    }
  }

  public static final class StatusRedisToken extends AbstractRedisToken<SafeString> {
    StatusRedisToken(SafeString value) {
      super(RedisTokenType.STATUS, value);
    }
    
    @Override
    public void accept(RedisTokenVisitor visitor)
    {
      visitor.status(this);
    }
  }

  public static final class ErrorRedisToken extends AbstractRedisToken<SafeString> {
    ErrorRedisToken(SafeString value) {
      super(RedisTokenType.ERROR, value);
    }
    
    @Override
    public void accept(RedisTokenVisitor visitor)
    {
      visitor.error(this);
    }
  }

  public static final class IntegerRedisToken extends AbstractRedisToken<Integer> {
    IntegerRedisToken(Integer value) {
      super(RedisTokenType.INTEGER, value);
    }
    
    @Override
    public void accept(RedisTokenVisitor visitor)
    {
      visitor.integer(this);
    }
  }

  public static final class ArrayRedisToken extends AbstractRedisToken<Collection<RedisToken>> {
    ArrayRedisToken(Collection<RedisToken> value) {
      super(RedisTokenType.ARRAY, unmodifiableList(new ArrayList<>(value)));
    }
    
    @Override
    public void accept(RedisTokenVisitor visitor)
    {
      visitor.array(this);
    }

    public int size() {
      return getValue().size();
    }
  }
}