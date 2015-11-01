/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

public interface IServerContext {

    int getPort();

    int getClients();

    ICommand getCommand(String name);

    <T> T getValue(String key);

    void putValue(String key, Object value);

}
