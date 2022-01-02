/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.RedisToken.array;
import static com.github.tonivade.resp.protocol.RedisToken.status;
import static com.github.tonivade.resp.protocol.RedisToken.string;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.RedisTokenType;

@ExtendWith(RespServerExtension.class)
public class RespClientTest {

  private static final String HOST = "localhost";
  private static final int PORT = 12345;
  private static final int TIMEOUT = 5000;

  private RespClient client;

  private RespCallback callback = mock(RespCallback.class);

  @BeforeEach
  public void setUp() {
    client = new RespClient(HOST, PORT, callback);
  }

  @Test
  public void onConnect() {
    client.start();

    verify(callback, timeout(1000)).onConnect();
  }

  @Test
  public void onMessage() {
    client.start();
    verify(callback, timeout(TIMEOUT)).onConnect();

    client.send(array(string("PING")));

    ArgumentCaptor<RedisToken> captor = ArgumentCaptor.forClass(RedisToken.class);

    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());

    assertThat(captor.getValue(), equalTo(status("PONG")));
  }

  @Test
  public void onBigMessage() {
    client.start();
    verify(callback, timeout(TIMEOUT)).onConnect();

    client.send(array(string("PING"), string(readBigFile())));

    ArgumentCaptor<RedisToken> captor = ArgumentCaptor.forClass(RedisToken.class);

    verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());

    RedisToken token = captor.getValue();
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
    client.start();
    verify(callback, timeout(TIMEOUT)).onConnect();

    client.stop();
    verify(callback, timeout(TIMEOUT)).onDisconnect();
  }

  @Test
  public void requireHostNonNull() {
    assertThrows(IllegalArgumentException.class, () -> new RespClient(null, 0, callback));
  }

  @Test
  public void requireHostNonEmpty() {
    assertThrows(IllegalArgumentException.class, () -> new RespClient("", 0, callback));
  }

  @Test
  public void requirePortLowerThan1024() {
    assertThrows(IllegalArgumentException.class, () -> new RespClient("localshot", 0, callback));
  }

  @Test
  public void requirePortGreaterThan65535() {
    assertThrows(IllegalArgumentException.class, () -> new RespClient("localshot", 987654321, callback));
  }

  @Test
  public void requireCallback() {
    assertThrows(IllegalArgumentException.class, () -> new RespClient("localhost", 12345, null));
  }
}
