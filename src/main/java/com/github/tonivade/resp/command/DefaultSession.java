/*
 * Copyright (c) 2015-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.util.Precondition.checkNonEmpty;

import com.github.tonivade.resp.StateHolder;
import com.github.tonivade.resp.protocol.RedisToken;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Optional;

public class DefaultSession implements Session {

  private final String id;

  private final ChannelHandlerContext ctx;

  private final StateHolder state = new StateHolder();

  public DefaultSession(String id, ChannelHandlerContext ctx) {
    this.id = checkNonEmpty(id);
    // it should be non null, but it seems that in some places it creates
    // a dummy session with a ctx null.
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

  @Override public InetSocketAddress getRemoteAddress() {
    return (InetSocketAddress) ctx.channel().remoteAddress();
  }

  @Override public InetSocketAddress getLocalAddress() {
    return (InetSocketAddress) ctx.channel().localAddress();
  }

  @Override
  public void putValue(String key, Object value) {
    state.putValue(key, value);
  }
}
