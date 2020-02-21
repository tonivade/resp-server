/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import com.github.tonivade.resp.protocol.RedisToken;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

interface Resp {
  void channel(SocketChannel channel);
  void connected(ChannelHandlerContext ctx);
  void disconnected(ChannelHandlerContext ctx);
  void receive(ChannelHandlerContext ctx, RedisToken message);
}
