/*
 * Copyright (c) 2016-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import java.util.function.Function;

import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ErrorRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StatusRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

class LambdaRedisTokenVisitor<T> implements RedisTokenVisitor<T> {

  private Function<ArrayRedisToken, T> onArray;
  private Function<StatusRedisToken, T> onStatus;
  private Function<StringRedisToken, T> onString;
  private Function<ErrorRedisToken, T> onError;
  private Function<IntegerRedisToken, T> onInteger;
  private Function<UnknownRedisToken, T> onUnknown;

  LambdaRedisTokenVisitor(
      Function<ArrayRedisToken, T> onArray,
      Function<StatusRedisToken, T> onStatus,
      Function<StringRedisToken, T> onString,
      Function<ErrorRedisToken, T> onError,
      Function<IntegerRedisToken, T> onInteger,
      Function<UnknownRedisToken, T> onUnknown) {
    this.onArray = onArray;
    this.onStatus = onStatus;
    this.onString = onString;
    this.onError = onError;
    this.onInteger = onInteger;
    this.onUnknown = onUnknown;
  }

  @Override
  public T array(ArrayRedisToken token) {
    return onArray.apply(token);
  }

  @Override
  public T status(StatusRedisToken token) {
    return onStatus.apply(token);
  }

  @Override
  public T string(StringRedisToken token) {
    return onString.apply(token);
  }

  @Override
  public T error(ErrorRedisToken token) {
    return onError.apply(token);
  }

  @Override
  public T unknown(UnknownRedisToken token) {
    return onUnknown.apply(token);
  }

  @Override
  public T integer(IntegerRedisToken token) {
    return onInteger.apply(token);
  }

  public static class Builder<T> {
    private Function<ArrayRedisToken, T> onArray;
    private Function<StatusRedisToken, T> onStatus;
    private Function<StringRedisToken, T> onString;
    private Function<ErrorRedisToken, T> onError;
    private Function<IntegerRedisToken, T> onInteger;
    private Function<UnknownRedisToken, T> onUnknown;

    public Builder<T> onArray(Function<ArrayRedisToken, T> onArray) {
      this.onArray = requireNonNull(onArray);
      return this;
    }

    public Builder<T> onStatus(Function<StatusRedisToken, T> onStatus) {
      this.onStatus = requireNonNull(onStatus);
      return this;
    }

    public Builder<T> onString(Function<StringRedisToken, T> onString) {
      this.onString = requireNonNull(onString);
      return this;
    }

    public Builder<T> onError(Function<ErrorRedisToken, T> onError) {
      this.onError = requireNonNull(onError);
      return this;
    }

    public Builder<T> onInteger(Function<IntegerRedisToken, T> onInteger) {
      this.onInteger = requireNonNull(onInteger);
      return this;
    }

    public Builder<T> onUnknown(Function<UnknownRedisToken, T> onUnknown) {
      this.onUnknown = requireNonNull(onUnknown);
      return this;
    }

    public RedisTokenVisitor<T> build() {
      return new LambdaRedisTokenVisitor<>(
          safe(onArray), safe(onStatus), safe(onString),
          safe(onError), safe(onInteger), safe(onUnknown));
    }

    private <X> Function<X, T> safe(Function<X, T> function) {
      return nonNull(function) ? function : x -> null;
    }
  }
}
