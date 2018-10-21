/*
 * Copyright (c) 2015-2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ErrorRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StatusRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;

public class RedisSerializer {
  private static final byte ARRAY = '*';
  private static final byte ERROR = '-';
  private static final byte INTEGER = ':';
  private static final byte SIMPLE_STRING = '+';
  private static final byte BULK_STRING = '$';

  private static final byte[] DELIMITER = new byte[] { '\r', '\n' };
  private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private ByteBufferBuilder builder = new ByteBufferBuilder();

  public byte[] encodeToken(RedisToken msg) {
    msg.accept(new AbstractRedisTokenVisitor<Void>() {
      @Override
      public Void string(StringRedisToken token) {
        addBulkStr(token.getValue());
        return null;
      }

      @Override
      public Void status(StatusRedisToken token) {
        addSimpleStr(token.getValue());
        return null;
      }

      @Override
      public Void integer(IntegerRedisToken token) {
        addInt(token.getValue());
        return null;
      }

      @Override
      public Void error(ErrorRedisToken token) {
        addError(token.getValue());
        return null;
      }

      @Override
      public Void array(ArrayRedisToken token) {
        addArray(token.getValue());
        return null;
      }
    });
    return builder.build();
  }

  private void addBulkStr(SafeString str) {
    if (str != null) {
      builder.append(BULK_STRING).append(str.length()).append(DELIMITER).append(str);
    } else {
      builder.append(BULK_STRING).append(-1);
    }
    builder.append(DELIMITER);
  }

  private void addSimpleStr(String str) {
    builder.append(SIMPLE_STRING).append(str.getBytes()).append(DELIMITER);
  }

  private void addInt(Integer value) {
    builder.append(INTEGER).append(value).append(DELIMITER);
  }

  private void addError(String str) {
    builder.append(ERROR).append(str.getBytes()).append(DELIMITER);
  }

  private void addArray(Sequence<RedisToken> array) {
    if (array != null) {
      builder.append(ARRAY).append(array.size()).append(DELIMITER);
      for (RedisToken token : array) {
        builder.append(new RedisSerializer().encodeToken(token));
      }
    } else {
      builder.append(ARRAY).append(0).append(DELIMITER);
    }
  }

  private static class ByteBufferBuilder {
    private static final int INITIAL_CAPACITY = 1024;

    private ByteBuffer buffer = ByteBuffer.allocate(INITIAL_CAPACITY);

    private ByteBufferBuilder append(int i) {
      append(String.valueOf(i));
      return this;
    }

    private ByteBufferBuilder append(String str) {
      append(str.getBytes(DEFAULT_CHARSET));
      return this;
    }

    private ByteBufferBuilder append(SafeString str) {
      append(str.getBytes());
      return this;
    }

    private ByteBufferBuilder append(byte[] buf) {
      ensureCapacity(buf.length);
      buffer.put(buf);
      return this;
    }

    public ByteBufferBuilder append(byte b) {
      ensureCapacity(1);
      buffer.put(b);
      return this;
    }

    private void ensureCapacity(int len) {
      if (buffer.remaining() < len) {
        growBuffer(len);
      }
    }

    private void growBuffer(int len) {
      int capacity = buffer.capacity() + Math.max(len, INITIAL_CAPACITY);
      buffer = ByteBuffer.allocate(capacity).put(build());
    }

    public byte[] build() {
      byte[] array = new byte[buffer.position()];
      buffer.rewind();
      buffer.get(array);
      return array;
    }
  }
}
