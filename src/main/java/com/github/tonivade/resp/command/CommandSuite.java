/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.server.EchoCommand;
import com.github.tonivade.resp.command.server.PingCommand;
import com.github.tonivade.resp.command.server.QuitCommand;
import com.github.tonivade.resp.command.server.TimeCommand;

import javaslang.control.Try;

public class CommandSuite {

  private static final Logger LOGGER = Logger.getLogger(CommandSuite.class.getName());

  private final Map<String, Class<?>> metadata = new HashMap<>();
  private final Map<String, ICommand> commands = new HashMap<>();

  private final NullCommand nullCommand = new NullCommand();

  private final CommandWrapperFactory factory;

  public CommandSuite() {
    this(new DefaultCommandWrapperFactory());
  }

  public CommandSuite(CommandWrapperFactory factory) {
    this.factory = factory;
    addCommand(PingCommand.class);
    addCommand(EchoCommand.class);
    addCommand(QuitCommand.class);
    addCommand(TimeCommand.class);
  }

  public ICommand getCommand(String name) {
    return commands.getOrDefault(name.toLowerCase(), nullCommand);
  }

  public boolean isPresent(String name, Class<? extends Annotation> annotationClass) {
    return getMetadata(name).isAnnotationPresent(annotationClass);
  }

  protected void addCommand(Class<?> clazz) {
    Try.of(clazz::newInstance)
       .onSuccess(this::processCommand)
       .onFailure(e -> LOGGER.log(Level.SEVERE, "error loading command: " + clazz.getName(), e));
  }

  protected void addCommand(String name, ICommand command) {
    commands.put(name.toLowerCase(), factory.wrap(command));
  }

  protected boolean contains(String name) {
    return commands.get(name) != null;
  }

  private void processCommand(Object command) {
    Class<?> clazz = command.getClass();
    Command annotation = clazz.getAnnotation(Command.class);
    if (annotation != null) {
      commands.put(annotation.value(), factory.wrap(command));
      metadata.put(annotation.value(), clazz);
    } else {
      LOGGER.warning(() -> "annotation not present at " + clazz.getName());
    }
  }

  private Class<?> getMetadata(String name) {
    return metadata.getOrDefault(name.toLowerCase(), Void.class);
  }
}
