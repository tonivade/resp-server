/*
 * Copyright (c) 2016-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.RedisToken.string;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RespSerializer {

  public RedisToken getValue(Object object) {
    if (isPrimitive().test(object)) {
      return getStringValue(object);
    }
    if (object instanceof Object[]) {
      return getArrayValue((Object[]) object);
    }
    if (object instanceof Number) {
      return getStringValue(object);
    }
    if (object instanceof String) {
      return getStringValue(object);
    }
    if (object instanceof Collection) {
      return getCollectionValue((Collection<?>) object);
    }
    if (object instanceof Map) {
      return getMapValue((Map<?, ?>) object);
    }
    return getObjectValue(object);
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
    try {
      return getFieldValue(object, field);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      return null;
    }
  }

  private Object getFieldValue(Object object, Field field)
      throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    return field.get(object);
  }

  private RedisToken getStringValue(Object value) {
    return string(String.valueOf(value));
  }

  private Predicate<Object> isPrimitive() {
    return object -> object.getClass().isPrimitive();
  }
}
