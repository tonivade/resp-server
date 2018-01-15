/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import java.util.Optional;

import com.github.tonivade.resp.StateHolder;
import com.github.tonivade.resp.protocol.RedisToken;

import io.netty.channel.ChannelHandlerContext;

public class DefaultSession implements Session {

  private final String id;

  private final ChannelHandlerContext ctx;

  private final StateHolder state = new StateHolder();

  public DefaultSession(String id, ChannelHandlerContext ctx) {
    this.id = id;
    this.ctx = ctx;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void publish(RedisToken msg) {
    ctx.writeAndFlush(msg);
  }

  @Override
  public void close() {
    ctx.close();
  }

  @Override
  public void destroy() {
    state.clear();
  }

  @Override
  public <T> Optional<T> getValue(String key) {
    return state.getValue(key);
  }

  @Override
  public <T> Optional<T> removeValue(String key) {
    return state.removeValue(key);
  }

  @Override
  public void putValue(String key, Object value) {
    state.putValue(key, value);
  }
}
