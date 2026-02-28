/*
 * Copyright (c) 2015-2026, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

public class DefaultCommandWrapperFactory implements CommandWrapperFactory {

  @Override
  public RespCommand wrap(RespCommand command) {
    return new CommandWrapper(command);
  }
}
