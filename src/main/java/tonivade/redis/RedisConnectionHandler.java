/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis;

import java.util.logging.Level;
import java.util.logging.Logger;

import tonivade.redis.protocol.RedisToken;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

@Sharable
public class RedisConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = Logger.getLogger(RedisConnectionHandler.class.getName());

    private final IRedis impl;

    public RedisConnectionHandler(IRedis impl) {
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
        LOGGER.log(Level.SEVERE, "channel inactive");
        impl.disconnected(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.log(Level.SEVERE, "uncaught exception", cause);
        impl.disconnected(ctx);
        ctx.close();
    }

}