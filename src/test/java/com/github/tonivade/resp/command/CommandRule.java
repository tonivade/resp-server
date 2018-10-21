/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.SafeString;

public class CommandRule {
  private Request request;
  private ServerContext server;
  private Session session;
  private RespCommand command;
  private RedisToken response;
  private final Object target;

  public CommandRule(Object target) {
    this.target = target;
  }

  public Request getRequest() {
    return request;
  }

  public RedisToken getResponse() {
    return response;
  }

  public void init() throws ParameterResolutionException {
    server = mock(ServerContext.class);
    request = mock(Request.class);
    session = mock(Session.class);

    when(request.getServerContext()).thenReturn(server);
    when(request.getSession()).thenReturn(session);
    when(session.getId()).thenReturn("localhost:12345");

    MockitoAnnotations.initMocks(target);

    try {
      command = target.getClass().getAnnotation(CommandUnderTest.class).value().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ParameterResolutionException("error", e);
    }
  }

  public CommandRule execute() {
    response = new CommandWrapper(command).execute(request);
    return this;
  }

  public CommandRule withParams(String... params) {
    if (params != null) {
      when(request.getParams()).thenReturn(ImmutableArray.from(safeAsList(params)));
      int i = 0;
      for (String param : params) {
        when(request.getParam(i++)).thenReturn(safeString(param));
      }
      when(request.getLength()).thenReturn(params.length);
      when(request.getOptionalParam(anyInt())).thenAnswer(new Answer<Optional<SafeString>>() {
        @Override
        public Optional<SafeString> answer(InvocationOnMock invocation) throws Throwable {
          Integer index = (Integer) invocation.getArguments()[0];
          if (index < params.length) {
            return Optional.of(safeString(params[index]));
          }
          return Optional.empty();
        }
      });
    }
    return this;
  }

  public void assertThat(RedisToken token) {
    Assert.assertThat(response, equalTo(token));
  }

  @SuppressWarnings("unchecked")
  public <T> T verify(Class<T> type) {
    if (type.equals(ServerContext.class)) {
      return (T) Mockito.verify(server);
    } else if (type.equals(Session.class)) {
      return (T) Mockito.verify(session);
    }
    throw new IllegalArgumentException();
  }
}
