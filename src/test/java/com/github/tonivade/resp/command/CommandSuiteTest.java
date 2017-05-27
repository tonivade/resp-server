/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.string;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.annotation.ParamLength;
import com.github.tonivade.resp.protocol.RedisToken;

public class CommandSuiteTest {

  private CommandSuite commandSuite;

  @Before
  public void setUp() {
    commandSuite = new CommandSuite();
  }

  @Test
  public void addCommandLambda() {
    String command = "test";
    commandSuite.addCommand(command, request -> string(request.getCommand()));

    RedisToken<?> response = commandSuite.getCommand(command).execute(request(command));

    assertThat(response, equalTo(string(command)));
  }

  @Test
  public void addCommandClassGood() {
    commandSuite.addCommand(GoodCommand.class);

    assertThat(commandSuite.contains("good"), is(true));
  }

  @Test
  public void addCommandClassBad() {
    commandSuite.addCommand(BadCommand.class);

    assertThat(commandSuite.contains("bad"), is(false));
  }

  @Test
  public void isPresent() throws Exception {
    commandSuite.addCommand(GoodCommand.class);

    assertThat(commandSuite.isPresent("good", Command.class), is(true));
  }

  @Test
  public void notPresent() throws Exception {
    commandSuite.addCommand(GoodCommand.class);

    assertThat(commandSuite.isPresent("good", ParamLength.class), is(false));
  }

  @Test
  public void getCommandNull() {
    RespCommand command = commandSuite.getCommand("notExists");

    assertThat(command, is(instanceOf(NullCommand.class)));
  }

  private Request request(String command) {
    return new DefaultRequest(null, null, safeString(command), emptyList());
  }

  @Command("good")
  static class GoodCommand implements RespCommand {
    @Override
    public RedisToken<?> execute(Request request) {
      return RedisToken.responseOk();
    }
  }

  @Command("bad")
  static class BadCommand implements RespCommand {
    private String string;

    public BadCommand(String string) {
      this.string = string;
    }

    @Override
    public RedisToken<?> execute(Request request) {
      return RedisToken.string(string);
    }
  }
}
