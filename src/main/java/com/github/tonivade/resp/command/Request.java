/*
 * Copyright (c) 2015-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import com.github.tonivade.resp.protocol.SafeString;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Request {
  String getCommand();
  Iterable<SafeString> getParams();
  SafeString getParam(int i);
  Optional<SafeString> getOptionalParam(int i);
  int getLength();
  boolean isEmpty();
  Session getSession();
  ServerContext getServerContext();
  boolean isExit();

  default Stream<SafeString> getParamsAsStream() {
    return StreamSupport.stream(getParams().spliterator(), false);
  }
}
