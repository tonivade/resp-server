/*
 * Copyright (c) 2015-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class CommandRuleExtension implements ParameterResolver, AfterEachCallback {

  private final static Namespace RESPSERVER = Namespace.create("com.github.tonivade.resp-server");

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(CommandRule.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    CommandRule commandRule = new CommandRule(
            parameterContext.getTarget().orElseThrow(() -> new ParameterResolutionException("no target")));
    extensionContext.getStore(RESPSERVER).put(CommandRule.class, commandRule);
    commandRule.init();
    return commandRule;
  }
  
  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    context.getStore(RESPSERVER).remove(CommandRule.class, CommandRule.class).close();
  }
}
