/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.error;

import com.github.tonivade.resp.protocol.RedisToken;

public class NullCommand implements ICommand {
  @Override
  public RedisToken execute(IRequest request) {
    return error("ERR unknown command '" + request.getCommand() + "'");
  }
}
