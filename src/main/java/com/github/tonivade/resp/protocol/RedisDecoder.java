/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
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
        int eol = findEndOfLine(buffer);
        int size = eol - buffer.readerIndex();
        return readString(buffer, size);
    }

    private SafeString readString(ByteBuf buffer, int size) {
        SafeString safeString = readBytes(buffer, size);
        buffer.skipBytes(2);
        return safeString;
    }

    private SafeString readBytes(ByteBuf buffer, int size) {
    	byte[] readedBytes = new byte[size];
    	buffer.readBytes(readedBytes);
		return new SafeString(readedBytes);
    }

    private static int findEndOfLine(final ByteBuf buffer) {
        int i = buffer.forEachByte(ByteBufProcessor.FIND_LF);
        if (i > 0 && buffer.getByte(i - 1) == '\r') {
            i--;
        }
        return i;
    }

    private RedisToken parseResponse(ChannelHandlerContext ctx, ByteBuf buffer) {
        RedisParser parser = new RedisParser(maxLength, new RedisSource() {
            @Override
            public SafeString readString(int size) {
                return RedisDecoder.this.readString(buffer, size);
            }

            @Override
            public SafeString readLine() {
                return RedisDecoder.this.readLine(ctx, buffer);
            }
        });

        RedisToken token = parser.parse();
        checkpoint();
        return token;
    }
}
