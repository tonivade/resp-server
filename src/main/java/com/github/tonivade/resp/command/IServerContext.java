/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

public interface IServerContext {

    int getPort();

    int getClients();

    ICommand getCommand(String name);

    <T> T getValue(String key);

    void putValue(String key, Object value);

    <T> T removeValue(String key);

}
