/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.SafeString.fromHexString;
import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class SafeStringTest {

  @Test
  void testBytes() {
    SafeString str = safeString("Hola Mundo!");

    assertThat(new SafeString(str.getBuffer()), is(str));
    assertThat(str.length(), is(11));
    assertThat(str.toHexString(), is("486f6c61204d756e646f21"));
    assertThat(str.toString(), is("Hola Mundo!"));
    assertThat(fromHexString(str.toHexString()), is(str));
  }

  @Test
  void testList() {
    List<SafeString> list = safeAsList("1", "2", "3");

    assertThat(list.size(), is(3));
    assertThat(list.get(0), is(safeString("1")));
    assertThat(list.get(1), is(safeString("2")));
    assertThat(list.get(2), is(safeString("3")));
  }

  @Test
  void testSet() {
    NavigableSet<SafeString> set = new TreeSet<>(safeAsList("1", "2", "3"));

    SortedSet<SafeString> result = set.subSet(safeString("2"), safeString("4"));

    assertThat(result.size(), is(2));
    Iterator<SafeString> iterator = result.iterator();
    assertThat(iterator.next(), is(safeString("2")));
    assertThat(iterator.next(), is(safeString("3")));
  }
}
