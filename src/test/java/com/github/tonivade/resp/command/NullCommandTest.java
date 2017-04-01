/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.error;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.tonivade.resp.protocol.RedisToken;

@RunWith(MockitoJUnitRunner.class)
public class NullCommandTest {
  @Mock
  private IRequest request;

  private final NullCommand nullCommand = new NullCommand();

  @Test
  public void execute() {
    when(request.getCommand()).thenReturn("notExists");

    RedisToken response = nullCommand.execute(request);

    assertThat(response, equalTo(error("ERR unknown command 'notExists'")));
  }
}
