/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.type.Option;

public class StateHolderTest {
  
  private static final String VALUE = "value";
  private static final String KEY = "key";

  private StateHolder state = new StateHolder();
  
  @BeforeEach
  public void setUp() {
    state.clear();
  }
  
  @Test
  public void exists() {
    state.putValue(KEY, VALUE);

    assertThat(state.getValue(KEY), equalTo(Option.some(VALUE)));
  }
  
  @Test
  public void notExists() {
    assertThat(state.getValue(KEY), equalTo(Option.none()));
  }
  
  @Test
  public void remove() {
    state.putValue(KEY, VALUE);

    assertThat(state.removeValue(KEY), equalTo(Option.some(VALUE)));
    assertThat(state.getValue(KEY), equalTo(Option.none()));
  }
  
  @Test
  public void clear() {
    state.putValue(KEY, VALUE);
    assertThat(state.getValue(KEY), equalTo(Option.some(VALUE)));

    state.clear();
    assertThat(state.getValue(KEY), equalTo(Option.none()));
  }

}
