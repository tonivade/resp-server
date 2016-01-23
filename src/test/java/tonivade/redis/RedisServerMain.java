/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis;

import tonivade.redis.command.CommandSuite;

public class RedisServerMain {

    public static void main(String[] args) {
        RedisServer redisServer = new RedisServer("localhost", 12345, new CommandSuite());

        redisServer.start();
    }

}
