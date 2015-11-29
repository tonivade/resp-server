/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command.server;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import tonivade.redis.command.CommandRule;
import tonivade.redis.command.CommandUnderTest;

@CommandUnderTest(TimeCommand.class)
public class TimeCommandTest {

    @Rule
    public final CommandRule rule = new CommandRule(this);

    @Captor
    private ArgumentCaptor<Collection<String>> captor;

    @Test
    public void testExecute() {
        rule.execute().verify().addArray(captor.capture());

        Collection<String> value = captor.getValue();

        Iterator<String> iterator = value.iterator();
        String secs = iterator.next();
        String mics = iterator.next();

        System.out.println(secs);
        System.out.println(mics);
    }

}
