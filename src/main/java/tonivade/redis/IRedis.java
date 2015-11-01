/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis;

import tonivade.redis.protocol.RedisToken;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

public interface IRedis {

    void channel(SocketChannel channel);

    void connected(ChannelHandlerContext ctx);

    void disconnected(ChannelHandlerContext ctx);

    void receive(ChannelHandlerContext ctx, RedisToken message);

}