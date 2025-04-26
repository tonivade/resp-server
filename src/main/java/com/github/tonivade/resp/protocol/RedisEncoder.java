/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RedisEncoder extends MessageToByteEncoder<RedisToken> {
  @Override
  protected void encode(ChannelHandlerContext ctx, RedisToken msg, ByteBuf out) throws Exception {
    out.writeBytes(RedisSerializer.encodeToken(msg));
  }
}
