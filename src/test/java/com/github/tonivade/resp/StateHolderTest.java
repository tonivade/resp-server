/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    assertThat(state.getValue(KEY), equalTo(Optional.of(VALUE)));
  }
  
  @Test
  public void notExists() {
    assertThat(state.getValue(KEY), equalTo(Optional.empty()));
  }
  
  @Test
  public void remove() {
    state.putValue(KEY, VALUE);

    assertThat(state.removeValue(KEY), equalTo(Optional.of(VALUE)));
    assertThat(state.getValue(KEY), equalTo(Optional.empty()));
  }
  
  @Test
  public void clear() {
    state.putValue(KEY, VALUE);
    assertThat(state.getValue(KEY), equalTo(Optional.of(VALUE)));

    state.clear();
    assertThat(state.getValue(KEY), equalTo(Optional.empty()));
  }

}
