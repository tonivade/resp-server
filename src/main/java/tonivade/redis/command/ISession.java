/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import io.netty.channel.ChannelHandlerContext;

public interface ISession {

    String getId();

    ChannelHandlerContext getContext();

    void destroy();

    <T> T getValue(String key);

    void putValue(String key, Object value);

}
