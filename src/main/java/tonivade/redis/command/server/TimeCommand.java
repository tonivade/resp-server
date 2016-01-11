/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command.server;

import static java.util.Arrays.asList;
import static tonivade.redis.protocol.RedisToken.string;

import java.time.Clock;
import java.util.List;

import tonivade.redis.annotation.Command;
import tonivade.redis.command.ICommand;
import tonivade.redis.command.IRequest;
import tonivade.redis.command.IResponse;
import tonivade.redis.protocol.RedisToken;

@Command("time")
public class TimeCommand implements ICommand {

    private static final int SCALE = 1000;

    @Override
    public void execute(IRequest request, IResponse response) {
        long currentTimeMillis = Clock.systemDefaultZone().millis();
        List<RedisToken> result = asList(string(seconds(currentTimeMillis)), string(microseconds(currentTimeMillis)));
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
