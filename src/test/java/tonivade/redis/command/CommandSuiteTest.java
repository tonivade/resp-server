/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class CommandSuiteTest {

    private CommandSuite commandSuite = new CommandSuite();

    @Test
    public void getCommandNull() {
        ICommand command =  commandSuite.getCommand("notExists");

        assertThat(command, is(NullCommand.class));
    }
}
