/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.protocol;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static tonivade.equalizer.Equalizer.equalizer;

import java.util.List;
import java.util.Objects;

public abstract class RedisToken {

    private static final String SEPARATOR = "=>";

    private final RedisTokenType type;

    private final Object value;

    private RedisToken(RedisTokenType type, Object value) {
        this.type = requireNonNull(type);
        this.value = requireNonNull(value);
    }

    public RedisTokenType getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        return equalizer(this).append((one, other) -> Objects.equals(one.value, other.value)).applyTo(obj);
    }

    @Override
    public String toString() {
        return type + SEPARATOR + value;
    }

    public static class UnknownRedisToken extends RedisToken {
        public UnknownRedisToken(String value) {
            super(RedisTokenType.UNKNOWN, value);
        }
    }

    public static class StringRedisToken extends RedisToken {
        public StringRedisToken(SafeString value) {
            super(RedisTokenType.STRING, value);
        }
    }

    public static class StatusRedisToken extends RedisToken {
        public StatusRedisToken(String value) {
            super(RedisTokenType.STATUS, value);
        }
    }

    public static class ErrorRedisToken extends RedisToken {
        public ErrorRedisToken(String value) {
            super(RedisTokenType.ERROR, value);
        }
    }

    public static class IntegerRedisToken extends RedisToken {
        public IntegerRedisToken(Integer value) {
            super(RedisTokenType.INTEGER, value);
        }
    }

    public static class ArrayRedisToken extends RedisToken {
        public ArrayRedisToken(List<RedisToken> value) {
            super(RedisTokenType.ARRAY, unmodifiableList(value));
        }

        public int size() {
            return this.<List<RedisToken>>getValue().size();
        }
    }

}