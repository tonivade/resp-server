/*
 * Copyright (c) 2015-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.protocol.RedisToken.array;
import static com.github.tonivade.resp.protocol.RedisToken.error;
import static com.github.tonivade.resp.protocol.RedisToken.status;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;

public class RedisParser implements Iterator<RedisToken> {

  private static final byte STRING_PREFIX = '$';
  private static final byte INTEGER_PREFIX = ':';
  private static final byte ERROR_PREFIX = '-';
  private static final byte STATUS_PREFIX = '+';
  private static final byte ARRAY_PREFIX = '*';

  private final int maxLength;
  private final RedisSource source;

  public RedisParser(int maxLength, RedisSource source) {
    this.maxLength = maxLength;
    this.source = requireNonNull(source);
  }

  @Override
  public boolean hasNext() {
    return source.available() > 0;
  }

  @Override
  public RedisToken next() {
    return parseToken(source.readLine());
  }

  public <T> Stream<T> stream(Iterator<T> iterator) {
    return StreamSupport.stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
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

  private RedisToken parseIntegerToken(SafeString line) {
    Integer value = Integer.valueOf(line.substring(1));
    return new IntegerRedisToken(value);
  }

  private RedisToken parseStringToken(SafeString line) {
    StringRedisToken token;
    int length = Integer.parseInt(line.substring(1));
    if (length > 0 && length < maxLength) {
      token = new StringRedisToken(source.readString(length));
    } else {
      token = new StringRedisToken(SafeString.EMPTY_STRING);
    }
    return token;
  }

  private RedisToken parseArray(int size) {
    List<RedisToken> array = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      array.add(parseToken(source.readLine()));
    }

    return array(array);
  }
}
