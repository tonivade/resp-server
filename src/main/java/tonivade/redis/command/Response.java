/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import java.util.Collection;
import java.util.Objects;

import tonivade.redis.protocol.RedisToken;
import tonivade.redis.protocol.SafeString;

public class Response implements IResponse {

    private boolean exit;

    private RedisToken token;

    @Override
    public IResponse addBulkStr(SafeString str) {
        token = RedisToken.string(str);
        return this;
    }

    @Override
    public IResponse addSimpleStr(String str) {
        token = RedisToken.status(str);
        return this;
    }

    @Override
    public IResponse addInt(int value) {
        token = RedisToken.integer(value);
        return this;
    }

    @Override
    public IResponse addInt(boolean value) {
        token = RedisToken.integer(value ? 1 : 0);
        return this;
    }

    @Override
    public IResponse addError(String str) {
        token = RedisToken.error(str);
        return this;
    }

    @Override
    public IResponse addArray(Collection<RedisToken> array) {
        if (array == null) {
            token = RedisToken.array();
        } else {
            token = RedisToken.array(array);
        }
        return this;
    }

    @Override
    public RedisToken build() {
        return token;
    }

    @Override
    public void exit() {
        this.exit = true;
    }

    @Override
    public boolean isExit() {
        return exit;
    }

    @Override
    public String toString() {
        return Objects.toString(token);
    }

}
