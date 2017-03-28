/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static java.util.stream.Collectors.toList;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.Predicates.instanceOf;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.RespSerializer;
import com.github.tonivade.resp.protocol.SafeString;

public class Response implements IResponse {

  private boolean exit;

  private RedisToken token;

  private RespSerializer serializer = new RespSerializer();
  
  public IResponse add(RedisToken token) {
    this.token = token;
    return this;
  }

  @Override
  public IResponse addBulkStr(SafeString str) {
    token = RedisToken.string(str);
    return this;
  }

  @Override
  public IResponse addSimpleStr(String str) {
    token = RedisToken.status(str);
    return this;
  }

  @Override
  public IResponse addInt(int value) {
    token = RedisToken.integer(value);
    return this;
  }

  @Override
  public IResponse addInt(boolean value) {
    token = RedisToken.integer(value ? 1 : 0);
    return this;
  }

  @Override
  public IResponse addError(String str) {
    token = RedisToken.error(str);
    return this;
  }

  @Override
  public IResponse addArray(Collection<?> array) {
    if (array == null) {
      token = RedisToken.array();
    } else {
      token = RedisToken.array(array.stream().map(this::parseToken).collect(toList()));
    }
    return this;
  }

  private RedisToken parseToken(Object value) {
    return Match(value).of(
        Case(instanceOf(Integer.class), RedisToken::integer),
        Case(instanceOf(Boolean.class), RedisToken::integer),
        Case(instanceOf(String.class), RedisToken::string),
        Case(instanceOf(SafeString.class), RedisToken::string),
        Case(instanceOf(RedisToken.class), Function.identity()));
  }

  @Override
  public IResponse addObject(Object object) {
    token = serializer.getValue(object);
    return this;
  }

  @Override
  public RedisToken build() {
    return token;
  }

  @Override
  public void exit() {
    this.exit = true;
  }

  @Override
  public boolean isExit() {
    return exit;
  }

  @Override
  public String toString() {
    return Objects.toString(token);
  }
}
