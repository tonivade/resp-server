/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.ICommand;
import com.github.tonivade.resp.command.IRequest;
import com.github.tonivade.resp.command.IResponse;

@Command("quit")
public class QuitCommand implements ICommand {

    @Override
    public void execute(IRequest request, IResponse response) {
        response.addSimpleStr(IResponse.RESULT_OK).exit();
    }

}
