/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

class RespInitializerHandler extends ChannelInitializer<SocketChannel> {

  private final Resp impl;

  RespInitializerHandler(Resp impl) {
    this.impl = impl;
  }

  @Override
  protected void initChannel(SocketChannel channel) throws Exception {
    impl.channel(channel);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    impl.disconnected(ctx);
  }
}
