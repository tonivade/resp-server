/*
 * Copyright (c) 2016-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.purefun.Function1.cons;
import static com.github.tonivade.purefun.Function1.identity;
import static com.github.tonivade.purefun.Matcher1.instanceOf;
import static com.github.tonivade.resp.protocol.RedisToken.string;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Pattern1;
import com.github.tonivade.purefun.type.Try;

public class RespSerializer {

  public RedisToken getValue(Object object) {
    return Pattern1.<Object, RedisToken>build()
        .when(isPrimitive())
          .then(this::getStringValue)
        .when(instanceOf(Object[].class))
          .then(array -> getArrayValue((Object[]) array))
        .when(instanceOf(Number.class))
          .then(this::getStringValue)
        .when(instanceOf(String.class))
          .then(this::getStringValue)
        .when(instanceOf(Collection.class))
          .then(collection -> getCollectionValue((Collection<?>) collection))
        .when(instanceOf(Map.class))
          .then(map -> getMapValue((Map<?, ?>) map))
        .otherwise()
          .then(this::getObjectValue)
        .apply(object);
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
    return Try.of(() -> getFieldValue(object, field)).fold(cons(null), identity());
  }

  private Object getFieldValue(Object object, Field field)
      throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    return field.get(object);
  }

  private RedisToken getStringValue(Object value) {
    return string(String.valueOf(value));
  }

  private Matcher1<Object> isPrimitive() {
    return object -> object.getClass().isPrimitive();
  }
}
