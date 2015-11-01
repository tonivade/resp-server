/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */

package tonivade.redis.command.server;

import tonivade.redis.annotation.Command;
import tonivade.redis.annotation.ParamLength;
import tonivade.redis.command.ICommand;
import tonivade.redis.command.IRequest;
import tonivade.redis.command.IResponse;

@Command("echo")
@ParamLength(1)
public class EchoCommand implements ICommand {

    @Override
    public void execute(IRequest request, IResponse response) {
        response.addBulkStr(request.getParam(0));
    }

}
