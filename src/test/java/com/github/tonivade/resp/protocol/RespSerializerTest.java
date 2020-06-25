/*
 * Copyright (c) 2020-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.RedisToken.array;
import static com.github.tonivade.resp.protocol.RedisToken.string;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import com.github.tonivade.resp.protocol.test.Data;

public class RespSerializerTest {
  private RespSerializer serializer = new RespSerializer();

  private final Data data = new Data(1, "value");
  private final RedisToken expected = array(string("id"), string("1"),
                                            string("value"), string("value"));

  @Test
  public void getValue() {
    RedisToken array = serializer.getValue(data);

    assertThat(array, equalTo(expected));
  }

  @Test
  public void getValueCollection() {
    RedisToken array = serializer.getValue(asList(data, data, data));

    assertThat(array, equalTo(array(expected, expected, expected)));
  }

  @Test
  public void getValueArray() {
    RedisToken array = serializer.getValue(new Object[] { data, data, data });

    assertThat(array, equalTo(array(expected, expected, expected)));
  }

  @Test
  public void getValueMap() {
    RedisToken array = serializer.getValue(singletonMap("key", "value"));

    assertThat(array, equalTo(array(string("key"), string("value"))));
  }
}
