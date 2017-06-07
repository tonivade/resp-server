/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tonivade.resp.protocol.RedisToken;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

@Sharable
public class RespConnectionHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOGGER = Logger.getLogger(RespConnectionHandler.class.getName());

  private final Resp impl;

  public RespConnectionHandler(Resp impl) {
    this.impl = impl;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    impl.connected(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    try {
      impl.receive(ctx, (RedisToken) msg);
    } finally {
      ReferenceCountUtil.release(msg);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    LOGGER.log(Level.FINE, "channel inactive");
    impl.disconnected(ctx);
    ctx.close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    LOGGER.log(Level.FINE, "uncaught exception", cause);
    impl.disconnected(ctx);
    ctx.close();
  }
}