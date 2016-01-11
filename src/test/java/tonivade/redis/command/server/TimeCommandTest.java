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
import tonivade.redis.protocol.RedisToken;

@CommandUnderTest(TimeCommand.class)
public class TimeCommandTest {

    @Rule
    public final CommandRule rule = new CommandRule(this);

    @Captor
    private ArgumentCaptor<Collection<RedisToken>> captor;

    @Test
    public void testExecute() {
        rule.execute().verify().addArray(captor.capture());

        Collection<RedisToken> value = captor.getValue();

        Iterator<RedisToken> iterator = value.iterator();
        RedisToken secs = iterator.next();
        RedisToken mics = iterator.next();

        System.out.println(secs);
        System.out.println(mics);
    }

}
