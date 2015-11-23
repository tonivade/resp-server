/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tonivade.redis.annotation.Command;
import tonivade.redis.command.server.EchoCommand;
import tonivade.redis.command.server.PingCommand;
import tonivade.redis.command.server.QuitCommand;
import tonivade.redis.command.server.TimeCommand;

public class CommandSuite {

    private static final Logger LOGGER = Logger.getLogger(CommandSuite.class.getName());

    private final Map<String, ICommand> commands = new HashMap<>();

    private final NullCommand nullCommand = new NullCommand();

    public CommandSuite() {
        addCommand(PingCommand.class);
        addCommand(EchoCommand.class);
        addCommand(QuitCommand.class);
        addCommand(TimeCommand.class);
    }

    protected void addCommand(Class<?> clazz) {
        try {
            Object command = clazz.newInstance();

            Command annotation = clazz.getAnnotation(Command.class);
            if (annotation != null) {
                commands.put(annotation.value(), wrap(command));
            } else {
                LOGGER.warning(() -> "annotation not present at " + clazz.getName());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "error loading command: " + clazz.getName(), e);
        }
    }

    protected ICommand wrap(Object command) {
        if (command instanceof ICommand) {
            return new CommandWrapper((ICommand) command);
        }
        throw new RuntimeException();
    }

    public ICommand getCommand(String name) {
        return commands.getOrDefault(name.toLowerCase(), nullCommand);
    }
}
