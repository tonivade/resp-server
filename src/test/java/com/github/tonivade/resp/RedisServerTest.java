/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.RedisToken.array;
import static com.github.tonivade.resp.protocol.RedisToken.string;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
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
import com.github.tonivade.resp.protocol.RedisTokenType;

public class RedisServerTest {

  private static final String HOST = "localhost";
  private static final int PORT = 12345;
  private static final int TIMEOUT = 1000;

  private RedisServer redisServer;

  private CommandSuite commands = new CommandSuite();

  private IRedisCallback callback = mock(IRedisCallback.class);

  @Before
  public void setUp() {
    redisServer = new RedisServer(HOST, PORT, commands);
    redisServer.start();
  }

  @After
  public void tearDown() {
    redisServer.stop();
  }

  @Test
  public void serverRespond() {
    RedisClient redisClient = createClient();

    redisClient.send(array(string("PING")));

    verifyResponse("PONG");
  }

  @Test
  public void clientDisconects() {
    RedisClient redisClient = createClient();

    redisClient.stop();

    verify(callback, timeout(TIMEOUT)).onDisconnect();
  }

  @Test
  public void serverDisconects() {
    RedisClient redisClient = createClient();

    redisServer.stop();

    verify(callback, timeout(TIMEOUT)).onDisconnect();

    redisClient.stop();
  }

  @Test(expected = NullPointerException.class)
  public void requireHost() {
    new RedisServer(null, 0, commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void requirePortLowerThan1024() {
    new RedisServer(HOST, 0, commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void requirePortGreaterThan65535() {
    new RedisServer(HOST, 987654321, commands);
  }

  @Test(expected = NullPointerException.class)
  public void requireCallback() {
    new RedisServer(HOST, PORT, null);
  }

  private RedisClient createClient() {
    RedisClient redisClient = new RedisClient(HOST, PORT, callback);
    redisClient.start();
    verify(callback, timeout(TIMEOUT)).onConnect();
    return redisClient;
  }

  private void verifyResponse(String response) {
    ArgumentCaptor<RedisToken<?>> captor = ArgumentCaptor.forClass(RedisToken.class);

    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());

    RedisToken<?> token = captor.getValue();
    assertThat(token.getType(), equalTo(RedisTokenType.STATUS));
    assertThat(token.getValue(), equalTo(safeString(response)));
  }
}
