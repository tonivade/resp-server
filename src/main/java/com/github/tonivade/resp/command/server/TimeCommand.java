/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.resp.command.server;

import static com.github.tonivade.resp.protocol.RedisToken.string;
import static java.util.Arrays.asList;

import java.time.Clock;
import java.util.List;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.command.ICommand;
import com.github.tonivade.resp.command.IRequest;
import com.github.tonivade.resp.command.IResponse;
import com.github.tonivade.resp.protocol.RedisToken;

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
