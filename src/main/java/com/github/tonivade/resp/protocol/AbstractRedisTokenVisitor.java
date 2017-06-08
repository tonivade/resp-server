package com.github.tonivade.resp.protocol;

import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ErrorRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StatusRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;

public abstract class AbstractRedisTokenVisitor<T> implements RedisTokenVisitor<T> {
  @Override
  public T array(ArrayRedisToken token) {
    return null;
  }

  @Override
  public T status(StatusRedisToken token) {
    return null;
  }

  @Override
  public T string(StringRedisToken token) {
    return null;
  }

  @Override
  public T error(ErrorRedisToken token) {
    return null;
  }

  @Override
  public T unknown(UnknownRedisToken token) {
    return null;
  }

  @Override
  public T integer(IntegerRedisToken token) {
    return null;
  }
}
