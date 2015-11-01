/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis;

import tonivade.redis.protocol.RedisToken;

public interface IRedisCallback {

    void onConnect();

    void onDisconnect();

    void onMessage(RedisToken token);

}
