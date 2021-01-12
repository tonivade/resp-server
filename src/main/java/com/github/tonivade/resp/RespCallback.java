/*
 * Copyright (c) 2015-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import com.github.tonivade.resp.protocol.RedisToken;

public interface RespCallback {
  void onConnect();
  void onDisconnect();
  void onMessage(RedisToken token);
}
