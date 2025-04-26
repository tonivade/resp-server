/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.ByteProcessor;

public class RedisDecoder extends ReplayingDecoder<Void> {

  private final int maxLength;

  public RedisDecoder(int maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {
    out.add(parseResponse(buffer));
  }

  private SafeString readLine(ByteBuf buffer) {
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
    int i = buffer.forEachByte(ByteProcessor.FIND_CRLF);
    if (i > 0 && buffer.getByte(i - 1) == '\r') {
      i--;
    }
    return i;
  }

  private RedisToken parseResponse(ByteBuf buffer) {
    RedisToken token = createParser(buffer).next();
    checkpoint();
    return token;
  }

  private RedisParser createParser(ByteBuf buffer) {
    return new RedisParser(maxLength, new NettyRedisSource(this, buffer));
  }

  private static class NettyRedisSource implements RedisSource {
    private RedisDecoder decoder;
    private ByteBuf buffer;

    public NettyRedisSource(RedisDecoder decoder, ByteBuf buffer) {
      this.decoder = decoder;
      this.buffer = buffer;
    }

    @Override
    public int available() {
      return decoder.actualReadableBytes();
    }

    @Override
    public SafeString readString(int size) {
      return decoder.readString(buffer, size);
    }

    @Override
    public SafeString readLine() {
      return decoder.readLine(buffer);
    }
  }
}
