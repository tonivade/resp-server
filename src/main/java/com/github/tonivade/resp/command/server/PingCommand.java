/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.ICommand;
import com.github.tonivade.resp.command.IRequest;
import com.github.tonivade.resp.command.IResponse;

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
