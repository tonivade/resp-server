/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.error;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.tonivade.resp.protocol.RedisToken;

public class NullCommandTest {

  private Request request = Mockito.mock(Request.class);

  private final NullCommand nullCommand = new NullCommand();

  @Test
  public void execute() {
    when(request.getCommand()).thenReturn("notExists");

    RedisToken response = nullCommand.execute(request);

    assertThat(response, equalTo(error("ERR unknown command 'notExists'")));
  }
}
