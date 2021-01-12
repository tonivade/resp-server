/*
 * Copyright (c) 2015-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import com.github.tonivade.purefun.data.ImmutableArray;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.resp.protocol.SafeString;

public interface Request {
  String getCommand();
  ImmutableArray<SafeString> getParams();
  SafeString getParam(int i);
  Option<SafeString> getOptionalParam(int i);
  int getLength();
  boolean isEmpty();
  Session getSession();
  ServerContext getServerContext();
  boolean isExit();
}
