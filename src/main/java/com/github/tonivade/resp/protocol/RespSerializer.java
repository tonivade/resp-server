/*
 * Copyright (c) 2016, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.Predicates.instanceOf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javaslang.control.Try;

public class RespSerializer {

    public RedisToken getValue(Object object) {
        return Match(object).of(
                Case(isPrimitive(), this::getStringValue),
                Case(instanceOf(Object[].class), this::getArrayValue),
                Case(instanceOf(Number.class), this::getStringValue),
                Case(instanceOf(String.class), this::getStringValue),
                Case(instanceOf(Collection.class), this::getCollectionValue),
                Case(instanceOf(Map.class), this::getMapValue),
                Case($(), this::getObjectValue));
    }

    private RedisToken getMapValue(Map<?, ?> map) {
        List<RedisToken> tokens = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            tokens.add(getValue(entry.getKey()));
            tokens.add(getValue(entry.getValue()));
        }
        return RedisToken.array(tokens);
    }

    private RedisToken getCollectionValue(Collection<?> collection) {
        List<RedisToken> tokens = new ArrayList<>();
        for (Object item : collection) {
            tokens.add(getValue(item));
        }
        return RedisToken.array(tokens);
    }

    private RedisToken getArrayValue(Object[] array) {
        List<RedisToken> tokens = new ArrayList<>();
        for (Object item : array) {
            tokens.add(getValue(item));
        }
        return RedisToken.array(tokens);
    }

    private RedisToken getObjectValue(Object object) {
        List<RedisToken> tokens = new ArrayList<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            tokens.add(RedisToken.string(field.getName()));
            tokens.add(getValue(Try.of(() -> getFieldValue(object, field)).get()));
        }
        return RedisToken.array(tokens);
    }

    private Object getFieldValue(Object object, Field field) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        return field.get(object);
    }

    private RedisToken getStringValue(Object value) {
        return RedisToken.string(String.valueOf(value));
    }

    private Predicate<? super Object> isPrimitive() {
        return object -> object.getClass().isPrimitive();
    }
}
