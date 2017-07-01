/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.HexUtil.toHexString;
import static com.github.tonivade.resp.protocol.SafeString.safeAsList;
import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

public class SafeStringTest {
  @Test
  public void testBytes() {
    SafeString str = safeString("Hola Mundo!");

    assertThat(new SafeString(str.getBuffer()), is(str));
    assertThat(str.length(), is(11));
    assertThat(toHexString(str.getBytes()), is("486F6C61204D756E646F21"));
    assertThat(str.toString(), is("Hola Mundo!"));
  }

  @Test
  public void testList() {
    List<SafeString> list = safeAsList("1", "2", "3");

    assertThat(list.size(), is(3));
    assertThat(list.get(0), is(safeString("1")));
    assertThat(list.get(1), is(safeString("2")));
    assertThat(list.get(2), is(safeString("3")));
  }

  @Test
  public void testSet() {
    NavigableSet<SafeString> set = new TreeSet<>(safeAsList("1", "2", "3"));

    SortedSet<SafeString> result = set.subSet(safeString("2"), safeString("4"));

    assertThat(result.size(), is(2));
    Iterator<SafeString> iterator = result.iterator();
    assertThat(iterator.next(), is(safeString("2")));
    assertThat(iterator.next(), is(safeString("3")));
  }
}