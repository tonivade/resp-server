/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import java.util.Collection;

import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.SafeString;

public interface IResponse {

    String RESULT_OK = "OK";
    String RESULT_ERROR = "ERR";

    IResponse addArray(Collection<?> array);

    IResponse addBulkStr(SafeString str);

    IResponse addSimpleStr(String str);

    IResponse addInt(int value);

    IResponse addInt(boolean value);

    IResponse addError(String str);

    RedisToken build();

    void exit();

    boolean isExit();

}