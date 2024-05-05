/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import com.github.tonivade.resp.protocol.RedisToken;

@FunctionalInterface
public interface RespCommand {
  RedisToken execute(Request request);
}
