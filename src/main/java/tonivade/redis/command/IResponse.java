/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import java.util.Collection;

import tonivade.redis.protocol.SafeString;

public interface IResponse {

    String RESULT_OK = "OK";
    String RESULT_ERROR = "ERR";

    IResponse addArray(Collection<?> array);

    IResponse addBulkStr(SafeString str);

    IResponse addSimpleStr(String str);

    IResponse addInt(SafeString str);

    IResponse addInt(int value);

    IResponse addInt(long value);

    IResponse addInt(boolean value);

    IResponse addError(String str);

    void exit();

    boolean isExit();

}