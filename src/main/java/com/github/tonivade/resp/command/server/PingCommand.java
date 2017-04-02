/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.ICommand;
import com.github.tonivade.resp.command.IRequest;
import com.github.tonivade.resp.protocol.RedisToken;

@Command("ping")
public class PingCommand implements ICommand {

  public static final String PONG = "PONG";

  @Override
  public RedisToken execute(IRequest request) {
    if (request.getLength() > 0) {
      return RedisToken.string(request.getParam(0));
    } else {
      return RedisToken.status(PONG);
    }
  }
}
