/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.protocol.RedisToken.string;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.tonivade.resp.protocol.RedisToken;

public class ResponseTest {
  @Test
  public void response() {
    Response response = new Response();
    
    response.add(RedisToken.string("hola"));
    
    assertThat(response.build(), equalTo(string("hola")));
  }
}
