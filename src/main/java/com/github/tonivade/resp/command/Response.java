/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import java.util.Objects;

import com.github.tonivade.resp.protocol.RedisToken;

public class Response implements IResponse {

  private boolean exit;

  private RedisToken token;

  public IResponse add(RedisToken token) {
    this.token = token;
    return this;
  }
  
  @Override
  public RedisToken build() {
    return token;
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
