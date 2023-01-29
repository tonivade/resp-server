/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class DefaultRequestTest {

  @Test
  void testRequest() {
    DefaultRequest request = new DefaultRequest(null, null, safeString("a"),
                                                safeAsList("1", "2", "3"));

    assertThat(request.getCommand(), is("a"));
    assertThat(request.getLength(), is(3));
    assertThat(request.getParams(), is(safeAsList("1", "2", "3")));
    assertThat(request.getParam(0), is(safeString("1")));
    assertThat(request.getParam(1), is(safeString("2")));
    assertThat(request.getParam(2), is(safeString("3")));
    assertThat(request.getParam(3), is(nullValue()));
    assertThat(request.getOptionalParam(2).isPresent(), is(true));
    assertThat(request.getOptionalParam(2).get(), is(safeString("3")));
    assertThat(request.getOptionalParam(3).isPresent(), is(false));
    assertThat(request.toString(), is("a[3]: [1, 2, 3]"));
  }
}
