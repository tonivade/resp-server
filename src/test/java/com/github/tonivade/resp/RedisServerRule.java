/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.tonivade.resp.RedisServer;
import com.github.tonivade.resp.command.CommandSuite;

public class RedisServerRule implements TestRule {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;

    private final RedisServer server;

    public RedisServerRule() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public RedisServerRule(String host, int port) {
        this.server = new RedisServer(host, port, new CommandSuite());
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    server.start();
                    base.evaluate();
                } finally {
                    server.stop();
                }
            }
        };
    }
}
