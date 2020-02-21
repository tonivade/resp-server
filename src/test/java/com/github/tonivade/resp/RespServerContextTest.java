/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static com.github.tonivade.resp.protocol.RedisToken.nullString;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.resp.command.CommandSuite;
import com.github.tonivade.resp.command.DefaultRequest;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.command.RespCommand;
import com.github.tonivade.resp.command.Session;

public class RespServerContextTest {

  private static final String HOST = "localhost";
  private static final int PORT = 12345;

  @Mock
  private CommandSuite commands;
  @Mock
  private RespCommand command;
  @Mock
  private Session session;
  @Mock
  private Function<String, Session> factory;

  private RespServerContext serverContext;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    serverContext = new RespServerContext(HOST, PORT, commands);
  }

  @Test
  public void processCommand() {
    Request request = newRequest("test");
    when(commands.getCommand(request.getCommand())).thenReturn(command);
    when(command.execute(request)).thenReturn(nullString());

    serverContext.processCommand(request);

    verify(command, timeout(1000)).execute(request);
    verify(session, timeout(1000)).publish(nullString());
  }

  @Test
  public void processCommandException() {
    Request request = newRequest("test");
    when(commands.getCommand(request.getCommand())).thenReturn(command);
    doThrow(RuntimeException.class).when(command).execute(request);

    serverContext.processCommand(request);

    verify(command, timeout(1000)).execute(request);
    verify(session, timeout(1000).atLeast(0)).publish(any());
  }

  @Test
  public void getHost() {
    assertThat(serverContext.getHost(), equalTo(HOST));
  }

  @Test
  public void getPort() {
    assertThat(serverContext.getPort(), equalTo(PORT));
  }

  @Test
  public void getClients() {
    when(factory.apply("key")).thenReturn(session);
    assertThat(serverContext.getClients(), equalTo(0));

    serverContext.getSession("key", factory);

    assertThat(serverContext.getClients(), equalTo(1));
    assertThat(serverContext.getSession("key"), equalTo(session));
  }

  @Test
  public void getSession() {
    when(factory.apply("key")).thenReturn(session);

    serverContext.getSession("key", factory);
    serverContext.getSession("key", factory);

    verify(factory, times(1)).apply("key");
  }

  @Test
  public void removeSession() {
    when(factory.apply("key")).thenReturn(session);

    serverContext.getSession("key", factory);
    serverContext.removeSession("key");

    assertThat(serverContext.getClients(), equalTo(0));
  }

  @Test
  public void removeSessionNotExists() {
    serverContext.removeSession("key");
  }

  public void getSessionNull() {
    assertThat(serverContext.getSession("key"), nullValue());
  }

  @Test
  public void requireHost() {
    assertThrows(NullPointerException.class, () -> new RespServerContext(null, 0, commands));
  }

  @Test
  public void requirePortLowerThan1024() {
    assertThrows(IllegalArgumentException.class, () -> new RespServerContext(HOST, 0, commands));
  }

  @Test
  public void requirePortGreaterThan65535() {
    assertThrows(IllegalArgumentException.class, () -> new RespServerContext(HOST, 91231231, commands));
  }

  @Test
  public void requireCallback() {
    assertThrows(NullPointerException.class, () -> new RespServerContext(HOST, PORT, null));
  }

  private Request newRequest(String command) {
    return new DefaultRequest(serverContext, session, safeString(command), ImmutableArray.empty());
  }
}
