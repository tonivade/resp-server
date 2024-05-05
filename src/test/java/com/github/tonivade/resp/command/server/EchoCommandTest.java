/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import static com.github.tonivade.resp.protocol.RedisToken.string;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tonivade.resp.command.CommandRuleExtension;
import com.github.tonivade.resp.command.CommandRule;
import com.github.tonivade.resp.command.CommandUnderTest;

@ExtendWith(CommandRuleExtension.class)
@CommandUnderTest(EchoCommand.class)
class EchoCommandTest {

  @Test
  void testExecute(CommandRule rule) {
    rule.withParams("test")
        .execute()
        .assertThat(string("test"));
  }
}
