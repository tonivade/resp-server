/*
 * Copyright (c) 2016-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

public class DefaultCommandWrapperFactory implements CommandWrapperFactory {
  @Override
  public ICommand wrap(Object command) {
    if (command instanceof ICommand) {
      return new CommandWrapper((ICommand) command);
    }
    throw new IllegalArgumentException("must implements command interface");
  }
}
