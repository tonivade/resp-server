/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class StateHolderTest {
  
  private StateHolder state = new StateHolder();
  
  @Before
  public void setUp() {
    state.clear();
  }
  
  @Test
  public void exists() {
    state.putValue("key", "value");

    assertThat(state.getValue("key"), equalTo(Optional.of("value")));
  }
  
  @Test
  public void notExists() {
    assertThat(state.getValue("key"), equalTo(Optional.empty()));
  }
  
  @Test
  public void remove() {
    state.putValue("key", "value");

    assertThat(state.removeValue("key"), equalTo(Optional.of("value")));
    assertThat(state.getValue("key"), equalTo(Optional.empty()));
  }
  
  @Test
  public void clear() {
    state.putValue("key", "value");
    assertThat(state.getValue("key"), equalTo(Optional.of("value")));

    state.clear();
    assertThat(state.getValue("key"), equalTo(Optional.empty()));
  }

}
