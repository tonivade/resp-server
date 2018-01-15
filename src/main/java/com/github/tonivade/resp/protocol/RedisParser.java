/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.RedisToken.error;
import static com.github.tonivade.resp.protocol.RedisToken.status;

import java.util.ArrayList;
import java.util.List;

import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;

public class RedisParser {

  private static final byte STRING_PREFIX = '$';
  private static final byte INTEGER_PREFIX = ':';
  private static final byte ERROR_PREFIX = '-';
  private static final byte STATUS_PREFIX = '+';
  private static final byte ARRAY_PREFIX = '*';

  private final int maxLength;
  private final RedisSource source;

  public RedisParser(int maxLength, RedisSource source) {
    this.maxLength = maxLength;
    this.source = source;
  }

  public RedisToken parse() {
    return parseToken(source.readLine());
  }

  private RedisToken parseToken(SafeString line) {
    RedisToken token = new UnknownRedisToken(SafeString.EMPTY_STRING);
    if (line != null && !line.isEmpty()) {
      if (line.startsWith(ARRAY_PREFIX)) {
        int size = Integer.parseInt(line.substring(1));
        token = parseArray(size);
      } else if (line.startsWith(STATUS_PREFIX)) {
        token = status(line.substring(1));
      } else if (line.startsWith(ERROR_PREFIX)) {
        token = error(line.substring(1));
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

  private IntegerRedisToken parseIntegerToken(SafeString line) {
    Integer value = Integer.valueOf(line.substring(1));
    return new IntegerRedisToken(value);
  }

  private StringRedisToken parseStringToken(SafeString line) {
    StringRedisToken token;
    int length = Integer.parseInt(line.substring(1));
    if (length > 0 && length < maxLength) {
      token = new StringRedisToken(source.readString(length));
    } else {
      token = new StringRedisToken(SafeString.EMPTY_STRING);
    }
    return token;
  }

  private ArrayRedisToken parseArray(int size) {
    List<RedisToken> array = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      array.add(parseToken(source.readLine()));
    }

    return new ArrayRedisToken(array);
  }

}
