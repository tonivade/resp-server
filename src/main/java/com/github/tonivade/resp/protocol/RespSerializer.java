/*
 * Copyright (c) 2016, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static java.lang.String.valueOf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.tonivade.resp.protocol.RedisToken;

public class RespSerializer {
    public RedisToken getValue(Object object) {
        if (object.getClass().isPrimitive()) {
            return RedisToken.string(valueOf(object));
        }
        if (String.class.isInstance(object)) {
            return RedisToken.string(valueOf(object));
        }
        if (Number.class.isInstance(object)) {
            return RedisToken.string(valueOf(object));
        }
        if (object.getClass().isArray()) {
            return getArrayValue(Object[].class.cast(object));
        }
        if (Collection.class.isInstance(object)) {
            return getCollectionValue(Collection.class.cast(object));
        }
        if (Map.class.isInstance(object)) {
            return getMapValue(Map.class.cast(object));
        }
        return getObjectValue(object);
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
            try {
                tokens.add(RedisToken.string(field.getName()));
                field.setAccessible(true);
                tokens.add(getValue(field.get(object)));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return RedisToken.array(tokens);
    }

}
