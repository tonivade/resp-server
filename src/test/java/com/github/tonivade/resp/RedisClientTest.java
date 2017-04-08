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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.RedisTokenType;

public class RedisClientTest {

  private static final String HOST = "localhost";
  private static final int PORT = 12345;
  private static final int TIMEOUT = 2000;

  @Rule
  public RedisServerRule redisServerRule = new RedisServerRule(HOST, PORT);

  private RedisClient redisClient;

  private IRedisCallback callback = mock(IRedisCallback.class);

  @Before
  public void setUp() {
    redisClient = new RedisClient(HOST, PORT, callback);
  }

  @Test
  public void onConnect() {
    redisClient.start();

    verify(callback, timeout(1000)).onConnect();
  }

  @Test
  public void onMessage() {
    redisClient.start();
    verify(callback, timeout(TIMEOUT)).onConnect();

    redisClient.send(array(string("PING")));

    ArgumentCaptor<RedisToken<?>> captor = ArgumentCaptor.forClass(RedisToken.class);

    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());

    RedisToken<?> token = captor.getValue();
    assertThat(token.getType(), equalTo(RedisTokenType.STATUS));
    assertThat(token.getValue(), equalTo(safeString("PONG")));
  }

  @Test
  public void onBigMessage() {
    redisClient.start();
    verify(callback, timeout(TIMEOUT)).onConnect();

    redisClient.send(array(string("PING"), string(readBigFile())));

    ArgumentCaptor<RedisToken<?>> captor = ArgumentCaptor.forClass(RedisToken.class);

    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());

    RedisToken<?> token = captor.getValue();
    assertThat(token.getType(), equalTo(RedisTokenType.STRING));
  }

  private String readBigFile() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      sb.append("lkjsdfkjaskjflskjf");
    }
    return sb.toString();
  }

  @Test
  public void onClientDisconnect() {
    redisClient.start();
    verify(callback, timeout(TIMEOUT)).onConnect();

    redisClient.stop();
    verify(callback, timeout(TIMEOUT)).onDisconnect();
  }

  @Test(expected = NullPointerException.class)
  public void requireHost() {
    new RedisClient(null, 0, callback);
  }

  @Test(expected = IllegalArgumentException.class)
  public void requirePortLowerThan1024() {
    new RedisClient("localshot", 0, callback);
  }

  @Test(expected = IllegalArgumentException.class)
  public void requirePortGreaterThan65535() {
    new RedisClient("localshot", 987654321, callback);
  }

  @Test(expected = NullPointerException.class)
  public void requireCallback() {
    new RedisClient("localhost", 12345, null);
  }
}
