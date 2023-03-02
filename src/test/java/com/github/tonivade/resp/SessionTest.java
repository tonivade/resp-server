/*
 * Copyright (c) 2015-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.RedisToken.string;
import static com.github.tonivade.resp.protocol.RedisTokenType.STRING;
import static java.lang.String.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.protocol.RedisToken;

class SessionTest {

  private static final String LADDR = "laddr";
  private static final String RADDR = "raddr";
  private static final String HOST = "localhost";
  private static final int TIMEOUT = 5000;

  private RespServer server;

  private RespClient client;
  private RespCallback callback = mock(RespCallback.class);

  @BeforeEach
  void setUp() {
    server = RespServer.builder().host(HOST).randomPort().commands(testSuite()).build();
    client = new RespClient(HOST, server.getPort(), callback);

    server.start();
    client.start();
    verify(callback, timeout(TIMEOUT)).onConnect();
  }

  @AfterEach
  void tearDown() {
    client.stop();
    verify(callback, timeout(TIMEOUT)).onDisconnect();
    server.stop();
  }

  @Test
  void testRemoteAddress() {
    client.send(RADDR);

    assertThat(awaitResponse().getType(), equalTo(STRING));
  }

  @Test
  void testLocalAddress() {
    client.send(LADDR);

    assertThat(awaitResponse(), equalTo(string(String.valueOf(server.getPort()))));
  }

  private RedisToken awaitResponse() {
    ArgumentCaptor<RedisToken> captor = ArgumentCaptor.forClass(RedisToken.class);
    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());
    return captor.getValue();
  }

  private CommandSuite testSuite() {
    return new CommandSuite() {{
      addCommand(RADDR, request -> string(valueOf(request.getSession().getRemoteAddress().getPort())));
      addCommand(LADDR, request -> string(valueOf(request.getSession().getLocalAddress().getPort())));
    }};
  }
}

