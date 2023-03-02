/*
 * Copyright (c) 2015-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static java.util.Objects.requireNonNull;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

class ConnectionListener implements ChannelFutureListener {

  private final RespClient client;

  ConnectionListener(RespClient client) {
    this.client = requireNonNull(client);
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    if (!future.isSuccess()) {
      EventLoop eventLoop = future.channel().eventLoop();
      eventLoop.schedule(client::start, 1L, TimeUnit.SECONDS);
    }
  }
}
