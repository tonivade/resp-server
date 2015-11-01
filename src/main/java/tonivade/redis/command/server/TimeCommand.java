/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */

package tonivade.redis.command.server;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.util.List;
import java.util.stream.Stream;

import tonivade.redis.annotation.Command;
import tonivade.redis.command.ICommand;
import tonivade.redis.command.IRequest;
import tonivade.redis.command.IResponse;

@Command("time")
public class TimeCommand implements ICommand {

    private static final int SCALE = 1000;

    @Override
    public void execute(IRequest request, IResponse response) {
        long currentTimeMillis = Clock.systemDefaultZone().millis();
        List<String> result = Stream.of(
                seconds(currentTimeMillis), microseconds(currentTimeMillis)).collect(toList());
        response.addArray(result);
    }

    private String seconds(long currentTimeMillis) {
        return String.valueOf(currentTimeMillis / SCALE);
    }

    // XXX: Java doesn't have microsecond accuracy
    private String microseconds(long currentTimeMillis) {
        return String.valueOf((currentTimeMillis % SCALE) * SCALE);
    }
}
