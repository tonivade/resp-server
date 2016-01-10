/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import tonivade.redis.protocol.RedisToken.ArrayRedisToken;
import tonivade.redis.protocol.RedisToken.ErrorRedisToken;
import tonivade.redis.protocol.RedisToken.IntegerRedisToken;
import tonivade.redis.protocol.RedisToken.StatusRedisToken;
import tonivade.redis.protocol.RedisToken.StringRedisToken;
import tonivade.redis.protocol.RedisToken.UnknownRedisToken;

public class RedisParser {

    private static final String STRING_PREFIX = "$";
    private static final String INTEGER_PREFIX = ":";
    private static final String ERROR_PREFIX = "-";
    private static final String STATUS_PREFIX = "+";
    private static final String ARRAY_PREFIX = "*";

    private final int maxLength;
    private final RedisSource source;

    public RedisParser(int maxLength, RedisSource source) {
        this.maxLength = maxLength;
        this.source = source;
    }

    public RedisToken parse() {
        return parseLine(source.readLine());
    }

    private RedisToken parseLine(String line) {
        RedisToken token = null;
        if (line != null && !line.isEmpty()) {
            if (line.startsWith(ARRAY_PREFIX)) {
                int size = Integer.parseInt(line.substring(1));
                token = parseArray(size);
            } else if (line.startsWith(STATUS_PREFIX)) {
                token = new StatusRedisToken(line.substring(1));
            } else if (line.startsWith(ERROR_PREFIX)) {
                token = new ErrorRedisToken(line.substring(1));
            } else if (line.startsWith(INTEGER_PREFIX)) {
                token = parseIntegerToken(line);
            } else if (line.startsWith(STRING_PREFIX)) {
                token = parseStringToken(line);
            } else {
                token = new UnknownRedisToken(line);
            }
        }
        return token;
    }

    private RedisToken parseIntegerToken(String line) {
        Integer value = Integer.valueOf(line.substring(1));
        return new IntegerRedisToken(value);
    }

    private RedisToken parseStringToken(String line) {
        RedisToken token;
        int length = Integer.parseInt(line.substring(1));
        if (length > 0 && length < maxLength) {
            ByteBuffer buffer = source.readBytes(length);
            token = new StringRedisToken(new SafeString(buffer));
            source.readLine();
        } else {
            token = new StringRedisToken(SafeString.EMPTY_STRING);
        }
        return token;
    }

    private ArrayRedisToken parseArray(int size) {
        List<RedisToken> array = new ArrayList<>(size);

        for (int i = 0 ; i < size; i++) {
            array.add(parseLine(source.readLine()));
        }

        return new ArrayRedisToken(array);
    }

}
