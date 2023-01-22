/*
 * Copyright (c) 2015-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.protocol;

import static com.github.tonivade.resp.util.Precondition.checkNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import com.github.tonivade.resp.util.Equal;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SafeString implements Comparable<SafeString>, Serializable {

  private static final long serialVersionUID = -8770835877491298225L;

  public static final SafeString EMPTY_STRING = new SafeString(new byte[] {});

  private static final Equal<SafeString> EQUAL = Equal.<SafeString>of().comparing(x -> x.buffer);
  private static final char[] CHARS = "0123456789abcdef".toCharArray();

  private transient ByteBuffer buffer;

  public SafeString(byte[] bytes) {
    this.buffer = ByteBuffer.wrap(checkNonNull(bytes));
  }

  public SafeString(ByteBuffer buffer) {
    this.buffer = checkNonNull(buffer);
  }

  public byte[] getBytes() {
    ByteBuffer copy = buffer.duplicate();
    byte[] bytes = new byte[copy.remaining()];
    copy.get(bytes);
    return bytes;
  }

  public ByteBuffer getBuffer() {
    return buffer.duplicate();
  }

  public int length() {
    return buffer.remaining();
  }

  @Override
  public int hashCode() {
    return Objects.hash(buffer);
  }

  @Override
  public boolean equals(Object obj) {
    return EQUAL.applyTo(this, obj);
  }

  @Override
  public int compareTo(SafeString other) {
    return compare(this.getBytes(), other.getBytes());
  }

  @Override
  public String toString() {
    return UTF_8.decode(buffer.duplicate()).toString();
  }

  public String toHexString() {
    StringBuilder sb = new StringBuilder();
    byte[] bytes = getBytes();
    for (int i = 0; i < bytes.length; i++) {
      int v = bytes[i] & 0xFF;
      sb.append(CHARS[v >>> 4]).append(CHARS[v & 0x0F]);
    }
    return sb.toString();
  }

  public static SafeString safeString(String str) {
    return new SafeString(UTF_8.encode(checkNonNull(str)));
  }

  public static SafeString fromHexString(String string) {
    byte[] array = new byte[string.length() / 2];
    for (int i = 0; i < array.length; i++) {
      array[i] = (byte) Integer.parseInt(string.substring((i * 2), (i * 2) + 2), 16);
    }
    return new SafeString(array);
  }

  public static List<SafeString> safeAsList(String... strings) {
    return Stream.of(checkNonNull(strings)).map(SafeString::safeString).collect(toList());
  }

  public static SafeString append(SafeString stringA, SafeString stringB) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(checkNonNull(stringA).length() + checkNonNull(stringB).length());
    byteBuffer.put(stringA.getBytes());
    byteBuffer.put(stringB.getBytes());
    ((Buffer) byteBuffer).rewind();
    return new SafeString(byteBuffer);
  }

  public boolean isEmpty() {
    return length() == 0;
  }

  public boolean startsWith(byte b) {
    byte first = buffer.get(0);
    return first == b;
  }

  public String substring(int i) {
    return toString().substring(i);
  }

  private static int compare(byte[] left, byte[] right) {
    for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
      int a = (left[i] & 0xff);
      int b = (right[j] & 0xff);
      if (a != b) {
        return a - b;
      }
    }
    return left.length - right.length;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    byte[] bytes = getBytes();
    out.writeInt(bytes.length);
    out.write(bytes);
  }

  private void readObject(ObjectInputStream input) throws IOException {
    int length = input.readInt();
    byte[] bytes = new byte[length];
    int read = input.read(bytes);
    if (read != length) {
      throw new IOException();
    }
    this.buffer = ByteBuffer.wrap(bytes);
  }
}
