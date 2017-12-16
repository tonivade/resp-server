/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RespServerExtension implements BeforeEachCallback, AfterEachCallback {

  private final RespServer server;

  public RespServerExtension() {
    this.server = RespServer.builder().build();
  }
  
  @Override
  public void beforeEach(ExtensionContext context) throws Exception
  {
    server.start();
  }
  
  @Override
  public void afterEach(ExtensionContext context) throws Exception
  {
    server.stop();
  }
}
