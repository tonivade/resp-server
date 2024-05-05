/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.util.Precondition.checkNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Collection;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ErrorRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StatusRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;
import io.netty.util.Recycler;

public class RedisSerializer {

  private static final Recycler<ByteBufferBuilder> RECYCLER = new Recycler<ByteBufferBuilder>() {
    @Override
    protected ByteBufferBuilder newObject(Recycler.Handle<ByteBufferBuilder> handle) {
      return new ByteBufferBuilder(handle);
    }
  };

  private static final byte ARRAY = '*';
  private static final byte ERROR = '-';
  private static final byte INTEGER = ':';
  private static final byte SIMPLE_STRING = '+';
  private static final byte BULK_STRING = '$';

  private static final byte[] DELIMITER = new byte[] { '\r', '\n' };

  public static byte[] encodeToken(RedisToken msg) {
    try (ByteBufferBuilder builder = RECYCLER.get()) {
      encodeToken(builder, msg);
      return builder.toArray();
    }
  }

  private static void encodeToken(ByteBufferBuilder builder, RedisToken msg) {
    switch (msg.getType()) {
      case ARRAY:
        Collection<RedisToken> array = ((ArrayRedisToken) msg).getValue();
        if (array != null) {
          builder.append(ARRAY).append(array.size()).append(DELIMITER);
          for (RedisToken token : array) {
            encodeToken(builder, token);
          }
        } else {
          builder.append(ARRAY).append(0).append(DELIMITER);
        }
        break;
      case STRING:
        SafeString string = ((StringRedisToken) msg).getValue();
        if (string != null) {
          builder.append(BULK_STRING).append(string.length()).append(DELIMITER).append(string);
        } else {
          builder.append(BULK_STRING).append(-1);
        }
        builder.append(DELIMITER);
        break;
      case STATUS:
        String status = ((StatusRedisToken) msg).getValue();
        builder.append(SIMPLE_STRING).append(status).append(DELIMITER);
        break;
      case INTEGER:
        Integer integer = ((IntegerRedisToken) msg).getValue();
        builder.append(INTEGER).append(integer).append(DELIMITER);
        break;
      case ERROR:
        String error = ((ErrorRedisToken) msg).getValue();
        builder.append(ERROR).append(error).append(DELIMITER);
        break;
      case UNKNOWN:
        throw new IllegalArgumentException(msg.toString());
    }
  }

  private static class ByteBufferBuilder implements AutoCloseable {

    private static final int INITIAL_CAPACITY = 1024;

    private ByteBuffer buffer = ByteBuffer.allocate(INITIAL_CAPACITY);

    private final Recycler.Handle<ByteBufferBuilder> handle;

    private ByteBufferBuilder(Recycler.Handle<ByteBufferBuilder> handle) {
      this.handle = checkNonNull(handle);
    }

    @Override
    public void close() {
      if (buffer.capacity() > INITIAL_CAPACITY) {
        buffer = ByteBuffer.allocate(INITIAL_CAPACITY);
      } else {
        buffer.clear();
      }
      handle.recycle(this);
    }

    private ByteBufferBuilder append(int i) {
      append(String.valueOf(i));
      return this;
    }

    private ByteBufferBuilder append(String str) {
      append(str.getBytes(UTF_8));
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
      buffer = ByteBuffer.allocate(capacity).put(toArray());
    }

    public byte[] toArray() {
      byte[] array = new byte[buffer.position()];
      ((Buffer) buffer).rewind();
      buffer.get(array);
      return array;
    }
  }
}
