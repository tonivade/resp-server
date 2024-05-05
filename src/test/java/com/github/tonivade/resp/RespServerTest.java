/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.RedisToken.array;
import static com.github.tonivade.resp.protocol.RedisToken.string;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.protocol.RedisToken;

public class RespServerTest {

  private static final String HOST = "localhost";
  private static final int PORT = 12345;
  private static final int TIMEOUT = 3000;

  private final RespServer respServer = RespServer.builder().host(HOST).port(12345).commands(new CommandSuite()).build();

  private RespCallback callback = mock(RespCallback.class);

  @BeforeEach
  public void setUp() {
    respServer.start();
  }

  @AfterEach
  public void tearDown() {
    respServer.stop();
  }

  @Test
  public void serverRespond() {
    RespClient redisClient = createClient();

    redisClient.send(array(string("PING")));

    verifyResponse("PONG");
  }

  @Test
  public void clientDisconects() {
    RespClient redisClient = createClient();

    redisClient.stop();

    verify(callback, timeout(TIMEOUT)).onDisconnect();
  }

  @Test
  public void serverDisconects() {
    RespClient redisClient = createClient();

    respServer.stop();

    verify(callback, timeout(TIMEOUT)).onDisconnect();

    redisClient.stop();
  }

  @Test
  public void serverStartsOnRandomPort() {
    RespServer server = RespServer.builder().randomPort().build();

    server.start();

    assertTrue(server.getPort() != 0);
  }

  private RespClient createClient() {
    RespClient redisClient = new RespClient(HOST, PORT, callback);
    redisClient.start();
    verify(callback, timeout(TIMEOUT)).onConnect();
    return redisClient;
  }

  private void verifyResponse(String response) {
    ArgumentCaptor<RedisToken> captor = ArgumentCaptor.forClass(RedisToken.class);

    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());

    assertThat(captor.getValue(), equalTo(RedisToken.status(response)));
  }
}
