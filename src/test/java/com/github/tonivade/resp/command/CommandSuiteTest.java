/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CommandSuiteTest {

    private CommandSuite commandSuite = new CommandSuite();

    @Test
    public void getCommandNull() {
        ICommand command =  commandSuite.getCommand("notExists");

        assertThat(command, is(instanceOf(NullCommand.class)));
    }
}
