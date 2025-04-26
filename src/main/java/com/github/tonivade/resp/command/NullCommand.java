/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.error;

import com.github.tonivade.resp.protocol.RedisToken;

class NullCommand implements RespCommand {
  @Override
  public RedisToken execute(Request request) {
    return error("ERR unknown command '" + request.getCommand() + "'");
  }
}
