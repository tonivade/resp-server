/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import java.util.List;
import java.util.Optional;

import tonivade.redis.protocol.SafeString;

public interface IRequest {

    String getCommand();

    List<SafeString> getParams();

    SafeString getParam(int i);

    Optional<SafeString> getOptionalParam(int i);

    int getLength();

    ISession getSession();

    IServerContext getServerContext();

}