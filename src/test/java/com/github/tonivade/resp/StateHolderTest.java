/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class StateHolderTest {

  private static final String VALUE = "value";
  private static final String KEY = "key";

  private StateHolder state = new StateHolder();

  @BeforeEach
  void setUp() {
    state.clear();
  }

  @Test
  void exists() {
    state.putValue(KEY, VALUE);

    assertThat(state.getValue(KEY), equalTo(Optional.of(VALUE)));
  }

  @Test
  void notExists() {
    assertThat(state.getValue(KEY), equalTo(Optional.empty()));
  }

  @Test
  void remove() {
    state.putValue(KEY, VALUE);

    assertThat(state.removeValue(KEY), equalTo(Optional.of(VALUE)));
    assertThat(state.getValue(KEY), equalTo(Optional.empty()));
  }

  @Test
  void clear() {
    state.putValue(KEY, VALUE);
    assertThat(state.getValue(KEY), equalTo(Optional.of(VALUE)));

    state.clear();
    assertThat(state.getValue(KEY), equalTo(Optional.empty()));
  }

}
