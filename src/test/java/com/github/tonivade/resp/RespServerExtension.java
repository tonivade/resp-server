/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.github.tonivade.resp.command.CommandSuite;

public class RespServerExtension implements BeforeEachCallback, AfterEachCallback {

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 12345;

  private final RespServer server;

  public RespServerExtension() {
    this(DEFAULT_HOST, DEFAULT_PORT);
  }

  public RespServerExtension(String host, int port) {
    this.server = new RespServer(new RespServerContext(host, port, new CommandSuite()));
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
