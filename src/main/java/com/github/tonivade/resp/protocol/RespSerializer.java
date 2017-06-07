/*
 * Copyright (c) 2016-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.RedisToken.string;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.vavr.control.Try;

public class RespSerializer {

  public RedisToken getValue(Object object) {
    return Match(object).of(
        Case($(isPrimitive()), this::getStringValue),
        Case($(instanceOf(Object[].class)), this::getArrayValue),
        Case($(instanceOf(Number.class)), this::getStringValue),
        Case($(instanceOf(String.class)), this::getStringValue),
        Case($(instanceOf(Collection.class)), this::getCollectionValue),
        Case($(instanceOf(Map.class)), this::getMapValue),
        Case($(), this::getObjectValue));
  }

  private RedisToken getMapValue(Map<?, ?> map) {
    return RedisToken.array(map.entrySet().stream()
        .flatMap(entry -> Stream.of(getValue(entry.getKey()), getValue(entry.getValue())))
        .collect(toList()));
  }

  private RedisToken getCollectionValue(Collection<?> collection) {
    return RedisToken.array(collection.stream().map(this::getValue).collect(toList()));
  }

  private RedisToken getArrayValue(Object[] array) {
    return RedisToken.array(Stream.of(array).map(this::getValue).collect(toList()));
  }

  private RedisToken getObjectValue(Object object) {
    return RedisToken.array(Stream.of(object.getClass().getDeclaredFields())
        .filter(field -> !field.getName().startsWith("$"))
        .flatMap(field -> Stream.of(string(field.getName()), getValue(tryGetFieldValue(object, field))))
        .collect(toList()));
  }

  private Object tryGetFieldValue(Object object, Field field) {
    return Try.of(() -> getFieldValue(object, field)).get();
  }

  private Object getFieldValue(Object object, Field field) 
      throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    return field.get(object);
  }

  private RedisToken getStringValue(Object value) {
    return string(String.valueOf(value));
  }

  private Predicate<? super Object> isPrimitive() {
    return object -> object.getClass().isPrimitive();
  }
}
