/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
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
