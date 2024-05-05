/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tonivade.resp.protocol.RedisToken;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

@Sharable
class RespConnectionHandler extends ChannelInboundHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RespConnectionHandler.class);

  private final Resp impl;

  RespConnectionHandler(Resp impl) {
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
    LOGGER.debug("channel inactive");
    impl.disconnected(ctx);
    ctx.close();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      Channel channel = ctx.channel();
      LOGGER.info("IdleStateEvent triggered, close channel {}", channel);
      impl.disconnected(ctx);
      ctx.close();
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    LOGGER.debug("uncaught exception", cause);
    impl.disconnected(ctx);
    ctx.close();
  }
}
