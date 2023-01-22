/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.string;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.annotation.ParamLength;
import com.github.tonivade.resp.protocol.RedisToken;
import java.util.Collections;

class CommandSuiteTest {

  private CommandSuite commandSuite;

  @BeforeEach
  void setUp() {
    commandSuite = new CommandSuite();
  }

  @Test
  void addCommandLambda() {
    String command = "test";
    commandSuite.addCommand(command, request -> string(request.getCommand()));

    RedisToken response = commandSuite.getCommand(command).execute(request(command));

    assertThat(response, equalTo(string(command)));
  }

  @Test
  void addCommand() {
    commandSuite.addCommand(GoodCommand::new);

    assertThat(commandSuite.contains("good"), is(true));
  }

  @Test
  void isPresent() {
    commandSuite.addCommand(GoodCommand::new);

    assertThat(commandSuite.isPresent("good", Command.class), is(true));
  }

  @Test
  void notPresent() {
    commandSuite.addCommand(GoodCommand::new);

    assertThat(commandSuite.isPresent("good", ParamLength.class), is(false));
  }

  @Test
  void getCommandNull() {
    RespCommand command = commandSuite.getCommand("notExists");

    assertThat(command, is(instanceOf(NullCommand.class)));
  }

  private Request request(String command) {
    return new DefaultRequest(null, null, safeString(command), Collections.emptyList());
  }

  @Command("good")
  static class GoodCommand implements RespCommand {
    @Override
    public RedisToken execute(Request request) {
      return RedisToken.responseOk();
    }
  }
}
