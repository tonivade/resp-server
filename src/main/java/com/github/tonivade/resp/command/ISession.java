/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import com.github.tonivade.resp.protocol.RedisToken;

public interface ISession {

    String getId();

    void publish(RedisToken msg);
    
    void close();

    void destroy();

    <T> T getValue(String key);

    void putValue(String key, Object value);

    <T> T removeValue(String key);

}
