/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;

import tonivade.redis.protocol.SafeString;

public class Response implements IResponse {

    private static final byte ARRAY = '*';
    private static final byte ERROR = '-';
    private static final byte INTEGER = ':';
    private static final byte SIMPLE_STRING = '+';
    private static final byte BULK_STRING = '$';

    private static final byte[] DELIMITER = new byte[] { '\r', '\n' };

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private boolean exit;

    private final ByteBufferBuilder builder = new ByteBufferBuilder();

    @Override
    public IResponse addBulkStr(SafeString str) {
        if (str != null) {
            builder.append(BULK_STRING).append(str.length()).append(DELIMITER).append(str);
        } else {
            builder.append(BULK_STRING).append(-1);
        }
        builder.append(DELIMITER);
        return this;
    }

    @Override
    public IResponse addSimpleStr(String str) {
        builder.append(SIMPLE_STRING).append(str);
        builder.append(DELIMITER);
        return this;
    }

    @Override
    public IResponse addInt(SafeString str) {
        builder.append(INTEGER).append(str);
        builder.append(DELIMITER);
        return this;
    }

    @Override
    public IResponse addInt(int value) {
        builder.append(INTEGER).append(value);
        builder.append(DELIMITER);
        return this;
    }

    @Override
    public IResponse addInt(long value) {
        builder.append(INTEGER).append(value);
        builder.append(DELIMITER);
        return this;
    }

    @Override
    public IResponse addInt(boolean value) {
        builder.append(INTEGER).append(value ? "1" : "0");
        builder.append(DELIMITER);
        return this;
    }

    @Override
    public IResponse addError(String str) {
        builder.append(ERROR).append(str);
        builder.append(DELIMITER);
        return this;
    }

    @Override
    public IResponse addArray(Collection<?> array) {
        if (array != null) {
            builder.append(ARRAY).append(array.size()).append(DELIMITER);
            for (Object value : array) {
                if (value instanceof Integer) {
                    addInt((Integer) value);
                } else if (value instanceof SafeString) {
                    addBulkStr((SafeString) value);
                } else if (value instanceof String) {
                    addSimpleStr((String) value);
                } else if (value instanceof byte[]) {
                    builder.append((byte[]) value);
                }
            }
        } else {
            builder.append(ARRAY).append(0).append(DELIMITER);
        }
        return this;
    }

    @Override
    public void exit() {
        this.exit = true;
    }

    @Override
    public boolean isExit() {
        return exit;
    }

    public byte[] getBytes() {
        return builder.build();
    }

    @Override
    public String toString() {
        return new String(getBytes(), DEFAULT_CHARSET);
    }

    private static class ByteBufferBuilder {

        private static final int INITIAL_CAPACITY = 1024;

        private ByteBuffer buffer = ByteBuffer.allocate(INITIAL_CAPACITY);

        public ByteBufferBuilder append(int i) {
            append(String.valueOf(i));
            return this;
        }

        public ByteBufferBuilder append(long l) {
            append(String.valueOf(l));
            return this;
        }

        public ByteBufferBuilder append(byte b) {
            ensureCapacity(1);
            buffer.put(b);
            return this;
        }

        public ByteBufferBuilder append(byte[] buf) {
            ensureCapacity(buf.length);
            buffer.put(buf);
            return this;
        }

        public ByteBufferBuilder append(String str) {
            append(DEFAULT_CHARSET.encode(str));
            return this;
        }

        public ByteBufferBuilder append(SafeString str) {
            append(str.getBuffer());
            return this;
        }

        public ByteBufferBuilder append(ByteBuffer b) {
            byte[] array = new byte[b.remaining()];
            b.get(array);
            append(array);
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
