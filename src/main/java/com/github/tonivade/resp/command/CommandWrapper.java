/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.resp.protocol.RedisToken.error;

import com.github.tonivade.resp.annotation.ParamLength;
import com.github.tonivade.resp.protocol.RedisToken;

public class CommandWrapper implements RespCommand {

  private Integer params;

  private Integer optionParams;

  private final RespCommand command;

  public CommandWrapper(RespCommand command) {
    this.command = checkNonNull(command);
    ParamLength length = command.getClass().getAnnotation(ParamLength.class);
    if (length != null) {
      this.params = length.value();
      this.optionParams = length.value() + length.option();
    }
  }

  @Override
  public RedisToken execute(Request request) {
    if (params != null && optionParams != null) {
      if (request.getLength() < params || request.getLength() > optionParams) {
        return error("ERR wrong number of arguments for '" + request.getCommand() + "' command");
      }
    }
    return command.execute(request);
  }
}
