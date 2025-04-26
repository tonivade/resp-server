/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import static com.github.tonivade.resp.protocol.RedisToken.responseOk;
import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.RespCommand;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.protocol.RedisToken;

@Command("quit")
public class QuitCommand implements RespCommand {

  @Override
  public RedisToken execute(Request request) {
    return responseOk();
  }
}
