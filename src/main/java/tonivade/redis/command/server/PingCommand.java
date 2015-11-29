/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command.server;

import tonivade.redis.annotation.Command;
import tonivade.redis.command.ICommand;
import tonivade.redis.command.IRequest;
import tonivade.redis.command.IResponse;

@Command("ping")
public class PingCommand implements ICommand {

    public static final String PONG = "PONG";

    @Override
    public void execute(IRequest request, IResponse response) {
        if (request.getLength() > 0) {
            response.addBulkStr(request.getParam(0));
        } else {
            response.addSimpleStr(PONG);
        }
    }

}
