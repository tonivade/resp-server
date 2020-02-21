/*
 * Copyright (c) 2016-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

public class DefaultCommandWrapperFactory implements CommandWrapperFactory {
  @Override
  public RespCommand wrap(Object command) {
    if (command instanceof RespCommand) {
      return new CommandWrapper((RespCommand) command);
    }
    throw new IllegalArgumentException("must implements command interface");
  }
}
