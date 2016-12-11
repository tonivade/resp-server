/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class RedisInitializerHandler extends ChannelInitializer<SocketChannel> {

    private final IRedis impl;

    public RedisInitializerHandler(IRedis impl) {
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
