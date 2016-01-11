/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package tonivade.redis.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static tonivade.redis.protocol.RedisToken.array;
import static tonivade.redis.protocol.RedisToken.error;
import static tonivade.redis.protocol.RedisToken.integer;
import static tonivade.redis.protocol.RedisToken.status;
import static tonivade.redis.protocol.RedisToken.string;
import static tonivade.redis.protocol.SafeString.safeString;

import org.junit.Before;
import org.junit.Test;

public class ResponseTest {

    private Response response;

    @Before
    public void setUp() throws Exception {
        response = new Response();
    }

    @Test
    public void testAddBulkStr() {
        assertThat(response.addBulkStr(safeString("test")).build(), is(string("test")));
    }

    @Test
    public void testAddSimpleStr() {
        assertThat(response.addSimpleStr("test").build(), is(status("test")));
    }

    @Test
    public void testAddIntInt() {
        assertThat(response.addInt(1).build(), is(integer(1)));
    }

    @Test
    public void testAddIntBooleanTrue() {
        assertThat(response.addInt(true).build(), is(integer(1)));
    }

    @Test
    public void testAddIntBooleanFalse() {
        assertThat(response.addInt(false).build(), is(integer(0)));
    }

    @Test
    public void testAddError() {
        assertThat(response.addError("ERROR").build(), is(error("ERROR")));
    }

    @Test
    public void testAddArrayNull() {
        assertThat(array(), is(array()));
        assertThat(response.addArray(null).build(), is(array()));
    }

}
