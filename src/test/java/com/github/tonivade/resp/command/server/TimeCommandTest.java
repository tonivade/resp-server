/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.resp.command.CommandRuleExtension;
import com.github.tonivade.resp.command.CommandRule;
import com.github.tonivade.resp.command.CommandUnderTest;
import com.github.tonivade.resp.protocol.RedisToken;

@ExtendWith(CommandRuleExtension.class)
@CommandUnderTest(TimeCommand.class)
public class TimeCommandTest {
  @Test
  public void testExecute(CommandRule rule) {
    rule.execute();

    RedisToken array = rule.getResponse();

    System.out.println(array);
  }
}
