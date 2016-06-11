/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class RedisDecoder extends ReplayingDecoder<Void> {

    private final int maxLength;

    public RedisDecoder(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        out.add(parseResponse(ctx, buffer));
    }

    private SafeString readLine(ChannelHandlerContext ctx, ByteBuf buffer) {
        int size = buffer.bytesBefore((byte) '\r');
        return readBytes(buffer, size);
    }

    private SafeString readBytes(ByteBuf buffer, int size) {
        SafeString safeString = new SafeString(buffer.readBytes(size).nioBuffer());
        buffer.skipBytes(2);
        return safeString;
    }

    private RedisToken parseResponse(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        RedisParser parser = new RedisParser(maxLength, new RedisSource() {
            @Override
            public SafeString readString(int size) {
                return RedisDecoder.this.readBytes(buffer, size);
            }

            @Override
            public SafeString readLine() {
                return RedisDecoder.this.readLine(ctx, buffer);
            }
        });

        return parser.parse();
    }

}
