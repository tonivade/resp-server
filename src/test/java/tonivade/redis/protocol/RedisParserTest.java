/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.protocol;

import static java.nio.ByteBuffer.wrap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.junit.Test;
import org.mockito.Mockito;

import tonivade.redis.protocol.RedisToken.UnknownRedisToken;

public class RedisParserTest {

    private RedisSource source = Mockito.mock(RedisSource.class);

    private RedisParser parser = new RedisParser(100000, source);

    private Charset utf8 = Charset.forName("UTF-8");

    private RedisToken intToken = RedisToken.integer(1);
    private RedisToken abcString = RedisToken.string("abc");
    private RedisToken pongString = RedisToken.status("pong");
    private RedisToken errorString = RedisToken.error("ERR");
    private RedisToken arrayToken = RedisToken.array(intToken, abcString);
    private RedisToken unknownString = new UnknownRedisToken("what?");

    @Test
    public void testBulkString() {
        when(source.readLine()).thenReturn("$3");
        when(source.readBytes(3)).thenReturn(wrap("abc".getBytes(utf8)));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(abcString));
    }

    @Test
    public void testSimpleString() {
        when(source.readLine()).thenReturn("+pong");

        RedisToken token = parser.parse();

        assertThat(token, equalTo(pongString));
    }

    @Test
    public void testInteger() {
        when(source.readLine()).thenReturn(":1");

        RedisToken token = parser.parse();

        assertThat(token, equalTo(intToken));
    }

    @Test
    public void testErrorString() {
        when(source.readLine()).thenReturn("-ERR");

        RedisToken token = parser.parse();

        assertThat(token, equalTo(errorString));
    }

    @Test
    public void testUnknownString() {
        when(source.readLine()).thenReturn("what?");

        RedisToken token = parser.parse();

        assertThat(token, equalTo(unknownString));
    }

    @Test
    public void testArray() {
        when(source.readLine()).thenReturn("*2", ":1", "$3");
        when(source.readBytes(3)).thenReturn(wrap("abc".getBytes(utf8)));

        RedisToken token = parser.parse();

        assertThat(token, equalTo(arrayToken));
    }

}
