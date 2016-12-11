/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NullCommandTest {

    @Mock
    private IRequest request;

    @Mock
    private IResponse response;

    @Test
    public void execute() {
        NullCommand nullCommand = new NullCommand();

        when(request.getCommand()).thenReturn("notExists");

        nullCommand.execute(request, response);

        verify(response).addError("ERR unknown command 'notExists'");
    }
}
