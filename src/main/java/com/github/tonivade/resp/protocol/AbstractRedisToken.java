/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.purefun.typeclasses.Equal.comparing;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import com.github.tonivade.purefun.typeclasses.Equal;

public abstract class AbstractRedisToken<T> implements RedisToken {

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
    return Equal.of(this)
        .append(comparing(AbstractRedisToken::getValue))
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
    public <T> T accept(RedisTokenVisitor<T> visitor) {
      return visitor.unknown(this);
    }
  }

  public static final class StringRedisToken extends AbstractRedisToken<SafeString> {
    StringRedisToken(SafeString value) {
      super(RedisTokenType.STRING, value);
    }

    @Override
    public <T> T accept(RedisTokenVisitor<T> visitor) {
      return visitor.string(this);
    }
  }

  public static final class StatusRedisToken extends AbstractRedisToken<String> {
    StatusRedisToken(String value) {
      super(RedisTokenType.STATUS, value);
    }

    @Override
    public <T> T accept(RedisTokenVisitor<T> visitor) {
      return visitor.status(this);
    }
  }

  public static final class ErrorRedisToken extends AbstractRedisToken<String> {
    ErrorRedisToken(String value) {
      super(RedisTokenType.ERROR, value);
    }

    @Override
    public <T> T accept(RedisTokenVisitor<T> visitor) {
      return visitor.error(this);
    }
  }

  public static final class IntegerRedisToken extends AbstractRedisToken<Integer> {
    IntegerRedisToken(Integer value) {
      super(RedisTokenType.INTEGER, value);
    }

    @Override
    public <T> T accept(RedisTokenVisitor<T> visitor) {
      return visitor.integer(this);
    }
  }

  public static final class ArrayRedisToken extends AbstractRedisToken<Collection<RedisToken>> {
    ArrayRedisToken(Collection<RedisToken> value) {
      super(RedisTokenType.ARRAY, unmodifiableList(new ArrayList<>(value)));
    }

    @Override
    public <T> T accept(RedisTokenVisitor<T> visitor) {
      return visitor.array(this);
    }

    public int size() {
      return getValue().size();
    }
  }
}
