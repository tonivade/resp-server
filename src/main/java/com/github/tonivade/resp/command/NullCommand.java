/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

public class NullCommand implements ICommand {

    @Override
    public void execute(IRequest request, IResponse response) {
        response.addError("ERR unknown command '" + request.getCommand() + "'");
    }

}
