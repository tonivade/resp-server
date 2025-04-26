/*
 * Copyright (c) 2015-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RespServerExtension implements BeforeAllCallback, AfterAllCallback {

  private final RespServer server;

  public RespServerExtension() {
    this.server = RespServer.builder().build();
  }
  
  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    server.start();
  }
  
  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    server.stop();
  }
}
