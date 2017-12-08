/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.RedisToken.array;
import static com.github.tonivade.resp.protocol.RedisToken.string;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.protocol.RedisToken;

public class RespServerTest {

  private static final String HOST = "localhost";
  private static final int PORT = 12345;
  private static final int TIMEOUT = 3000;

  private RespServer redisServer;

  private CommandSuite commands = new CommandSuite();

  private RespCallback callback = mock(RespCallback.class);

  @Before
  public void setUp() {
    redisServer = new RespServer(new RespServerContext(HOST, PORT, commands));
    redisServer.start();
  }

  @After
  public void tearDown() {
    redisServer.stop();
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

    redisServer.stop();

    verify(callback, timeout(TIMEOUT)).onDisconnect();

    redisClient.stop();
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
