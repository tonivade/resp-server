/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.github.tonivade.resp.protocol.RedisToken;

class NullCommandTest {

  @Test
  void execute() {
    Request request = mock(Request.class);
    when(request.getCommand()).thenReturn("notExists");

    RedisToken response = new NullCommand().execute(request);

    assertThat(response, equalTo(error("ERR unknown command 'notExists'")));
  }
}
