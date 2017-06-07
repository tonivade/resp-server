package com.github.tonivade.resp.protocol;

import com.github.tonivade.resp.protocol.AbstractRedisToken.ArrayRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.ErrorRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.IntegerRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StatusRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.StringRedisToken;
import com.github.tonivade.resp.protocol.AbstractRedisToken.UnknownRedisToken;

public abstract class AbstractRedisTokenVisitor implements RedisTokenVisitor {

  @Override
  public void array(ArrayRedisToken token) {
    
  }

  @Override
  public void status(StatusRedisToken token) {
    
  }

  @Override
  public void string(StringRedisToken token) {
    
  }

  @Override
  public void error(ErrorRedisToken token) {
    
  }

  @Override
  public void unknown(UnknownRedisToken token) {
    
  }

  @Override
  public void integer(IntegerRedisToken token) {
    
  }
}
