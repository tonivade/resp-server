/*
 * Copyright (c) 2016-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

public interface CommandWrapperFactory {
  RespCommand wrap(Object command);
}
