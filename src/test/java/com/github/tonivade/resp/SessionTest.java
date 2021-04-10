/*
 * Copyright (c) 2015-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.RedisTokenType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class SessionTest {

  private static final String LADDR = "laddr";
  private static final String RADDR = "raddr";
  private static final String HOST = "localhost";
  private static final int PORT = 12345;
  private static final int TIMEOUT = 2000;

  private RespServer server;

  private RespClient client;
  private RespCallback callback = mock(RespCallback.class);

  @BeforeEach
  void setUp() {
    server = RespServer.builder().host(HOST).port(PORT).commands(testSuite()).build();
    client = new RespClient(HOST, PORT, callback);

    server.start();
    client.start();

    verify(callback, timeout(TIMEOUT)).onConnect();
  }

  @AfterEach
  void tearDown() {
    client.stop();
    server.stop();
  }

  @Test
  void testRemoteAddress() {
    client.send(RADDR);

    assertThat(awaitResponse().getType(), equalTo(RedisTokenType.STRING));
  }

  @Test
  void testLocalAddress() {
    client.send(LADDR);

    assertThat(awaitResponse(), equalTo(RedisToken.string("12345")));
  }

  private RedisToken awaitResponse() {
    ArgumentCaptor<RedisToken> captor = ArgumentCaptor.forClass(RedisToken.class);
    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());
    return captor.getValue();
  }

  private CommandSuite testSuite() {
    return new CommandSuite() {{
      addCommand(RADDR, request -> RedisToken.string(String.valueOf(request.getSession().getRemoteAddress().getPort())));
      addCommand(LADDR, request -> RedisToken.string(String.valueOf(request.getSession().getLocalAddress().getPort())));
    }};
  }
}

