/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import com.github.tonivade.resp.annotation.ParamLength;

public class CommandWrapper implements ICommand {

  private int params;

  private final ICommand command;

  public CommandWrapper(ICommand command) {
    this.command = command;
    ParamLength length = command.getClass().getAnnotation(ParamLength.class);
    if (length != null) {
      this.params = length.value();
    }
  }

  @Override
  public void execute(IRequest request, IResponse response) {
    if (request.getLength() < params) {
      response.addError("ERR wrong number of arguments for '" + request.getCommand() + "' command");
    } else {
      command.execute(request, response);
    }
  }

}
