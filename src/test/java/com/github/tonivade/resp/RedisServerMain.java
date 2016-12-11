/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import com.github.tonivade.resp.RedisServer;
import com.github.tonivade.resp.command.CommandSuite;

public class RedisServerMain {

    public static void main(String[] args) {
        RedisServer redisServer = new RedisServer("localhost", 12345, new CommandSuite());

        redisServer.start();
    }

}
