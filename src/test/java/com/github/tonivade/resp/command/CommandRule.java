/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
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
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.SafeString;

public class CommandRule implements TestRule {

  private IRequest request;

  private IServerContext server;

  private ISession session;

  private final Object target;

  private ICommand command;

  private RedisToken response;

  public CommandRule(Object target) {
    this.target = target;
  }

  public IRequest getRequest() {
    return request;
  }
  
  public RedisToken getResponse() {
    return response;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        server = mock(IServerContext.class);
        request = mock(IRequest.class);
        session = mock(ISession.class);

        when(request.getServerContext()).thenReturn(server);
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn("localhost:12345");

        MockitoAnnotations.initMocks(target);

        command = target.getClass().getAnnotation(CommandUnderTest.class).value().newInstance();

        base.evaluate();
      }
    };
  }

  public CommandRule execute() {
    response = new CommandWrapper(command).execute(request);
    return this;
  }

  public CommandRule withParams(String... params) {
    if (params != null) {
      when(request.getParams()).thenReturn(safeAsList(params));
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

  public void then(RedisToken token) {
    Assert.assertThat(response, equalTo(token));
  }

  @SuppressWarnings("unchecked")
  public <T> T verify(Class<T> type) {
    if (type.equals(IServerContext.class)) {
      return (T) Mockito.verify(server);
    } else if (type.equals(ISession.class)) {
      return (T) Mockito.verify(session);
    }
    throw new IllegalArgumentException();
  }

}
