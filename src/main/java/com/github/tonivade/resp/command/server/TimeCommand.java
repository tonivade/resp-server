/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import static com.github.tonivade.resp.protocol.RedisToken.array;
import static com.github.tonivade.resp.protocol.RedisToken.string;

import java.time.Clock;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.RespCommand;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.protocol.RedisToken;

@Command("time")
public class TimeCommand implements RespCommand {

  private static final int SCALE = 1000;

  @Override
  public RedisToken execute(Request request) {
    long currentTimeMillis = Clock.systemDefaultZone().millis();
    return array(string(seconds(currentTimeMillis)), string(microseconds(currentTimeMillis)));
  }

  private String seconds(long currentTimeMillis) {
    return String.valueOf(currentTimeMillis / SCALE);
  }

  // XXX: Java doesn't have microsecond accuracy
  private String microseconds(long currentTimeMillis) {
    return String.valueOf((currentTimeMillis % SCALE) * SCALE);
  }
}
