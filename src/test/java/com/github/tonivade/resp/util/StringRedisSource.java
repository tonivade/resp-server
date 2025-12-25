package com.github.tonivade.resp.util;

import com.github.tonivade.resp.protocol.RedisSource;
import com.github.tonivade.resp.protocol.SafeString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A test implementation of {@link RedisSource} that reads from a string-based input.
 * <p>
 * This class is primarily intended for testing purposes.
 * Its design implies the following usage:
 * 0. It is not thread safe.
 * 1. It is initialized with arbitrary RESP data in string representation.
 * 2. Each {@link RedisSource} read method call will sequentially consume init data.
 * 3. If all init data has been consumed, effective read method calls will throw an exception.
 */
public class StringRedisSource implements RedisSource {
  private ByteArrayInputStream data;

  /**
   * Initializes the source with raw RESP data represented as string.
   * <p>
   * This method can receive one or more strings to append to the initial raw data, which is done for convenience.
   * All control sequences like {@code "\r\n"} must be explicitely present in astring.
   * This method can be executed multiple times, and each cal will re-init the whole state.
   * </p>
   * See <a href="https://redis.io/docs/latest/develop/reference/protocol-spec/">RESP specification</a> for examples.
   *
   * @param rawResp     the initial raw Redis protocol string
   * @param moreRawResp additional raw Redis protocol strings to append
   */
  public void init(String rawResp, String... moreRawResp) {
    StringBuilder combined = new StringBuilder(rawResp);
    for (String resp : moreRawResp) {
      combined.append(resp);
    }
    this.data = new ByteArrayInputStream(combined.toString().getBytes(StandardCharsets.US_ASCII));
  }

  @Override
  public int available() {
    return data.available();
  }

  @Override
  public SafeString readLine() {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    while (true) {
      int b = readByte();
      if (b != '\r') {
        buffer.write(b);
        continue;
      }

      b = readByte();
      if (b != '\n') {
        buffer.write(b);
        continue;
      }

      return new SafeString(buffer.toByteArray());
    }
  }

  @Override
  public SafeString readString(int length) {
    byte[] buffer = new byte[length];
    try {
      int actualLength = data.read(buffer);
      if (actualLength != length) {
        int left = length - actualLength;
        throw new IllegalStateException("No more data, expected at least `" + left + "` more byte.");
      }
      if (readByte() != '\r' || readByte() != '\n') {
        throw new IllegalStateException("No CRLF at the end of string.");
      }
      return new SafeString(buffer);
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected problem.", e);
    }
  }

  private int readByte() {
    int b = data.read();
    if (b < 0) {
      throw new IllegalStateException("No more data, expected at least `1` more byte.");
    }
    return b;
  }
}
