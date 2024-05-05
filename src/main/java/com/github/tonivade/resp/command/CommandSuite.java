/*
 * Copyright (c) 2015-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import static com.github.tonivade.resp.util.Precondition.checkNonNull;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.server.EchoCommand;
import com.github.tonivade.resp.command.server.PingCommand;
import com.github.tonivade.resp.command.server.QuitCommand;
import com.github.tonivade.resp.command.server.TimeCommand;

public class CommandSuite {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandSuite.class);

  private final Map<String, Class<?>> metadata = new HashMap<>();
  private final Map<String, RespCommand> commands = new HashMap<>();

  private final NullCommand nullCommand = new NullCommand();

  private final CommandWrapperFactory factory;

  public CommandSuite() {
    this(new DefaultCommandWrapperFactory());
  }

  public CommandSuite(CommandWrapperFactory factory) {
    this.factory = checkNonNull(factory);
    addCommand(PingCommand::new);
    addCommand(EchoCommand::new);
    addCommand(QuitCommand::new);
    addCommand(TimeCommand::new);
  }

  public RespCommand getCommand(String name) {
    return commands.getOrDefault(name.toLowerCase(), nullCommand);
  }

  public boolean isPresent(String name, Class<? extends Annotation> annotationClass) {
    return getMetadata(name).isAnnotationPresent(annotationClass);
  }

  public boolean contains(String name) {
    return commands.get(name) != null;
  }

  protected void addCommand(Supplier<?> newInstance) {
    try {
      processCommand(newInstance.get());
    } catch(Exception e) {
      LOGGER.error("error loading command", e);
    }
  }

  protected void addCommand(String name, RespCommand command) {
    commands.put(name.toLowerCase(), factory.wrap(command));
  }

  private void processCommand(Object command) {
    Class<?> clazz = command.getClass();
    Command annotation = clazz.getAnnotation(Command.class);
    if (annotation != null) {
      commands.put(annotation.value().toLowerCase(), factory.wrap(command));
      metadata.put(annotation.value().toLowerCase(), clazz);
    } else {
      LOGGER.warn("annotation not present at {}", clazz.getName());
    }
  }

  private Class<?> getMetadata(String name) {
    return metadata.getOrDefault(name.toLowerCase(), Void.class);
  }
}
