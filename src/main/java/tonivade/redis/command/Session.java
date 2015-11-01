/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

public class Session implements ISession {

    private final String id;

    private final ChannelHandlerContext ctx;

    private final Map<String, Object> state = new HashMap<>();

    public Session(String id, ChannelHandlerContext ctx) {
        super();
        this.id = id;
        this.ctx = ctx;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ChannelHandlerContext getContext() {
        return ctx;
    }

    @Override
    public void destroy() {

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) state.get(key);
    }

    @Override
    public void putValue(String key, Object value) {
        state.put(key, value);
    }

}
