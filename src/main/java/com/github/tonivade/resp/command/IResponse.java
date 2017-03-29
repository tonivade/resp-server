/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import com.github.tonivade.resp.protocol.RedisToken;

public interface IResponse {
  String RESULT_OK = "OK";
  String RESULT_ERROR = "ERR";
  IResponse add(RedisToken token);
  RedisToken build();
  boolean isExit();
}