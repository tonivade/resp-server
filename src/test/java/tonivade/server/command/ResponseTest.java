/*
 * Copyright (c) 2015, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */

package tonivade.server.command;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static tonivade.redis.protocol.SafeString.safeString;

import org.junit.Before;
import org.junit.Test;

import tonivade.redis.command.Response;

public class ResponseTest {

    private Response response;

    @Before
    public void setUp() throws Exception {
        response = new Response();
    }

    @Test
    public void testAddBulkStr() {
        assertThat(response.addBulkStr(safeString("test")).toString(), is("$4\r\ntest\r\n"));
    }

    @Test
    public void testAddSimpleStr() {
        assertThat(response.addSimpleStr("test").toString(), is("+test\r\n"));
    }

    @Test
    public void testAddIntString() {
        assertThat(response.addInt(safeString("1")).toString(), is(":1\r\n"));
    }

    @Test
    public void testAddIntInt() {
        assertThat(response.addInt(1).toString(), is(":1\r\n"));
    }

    @Test
    public void testAddIntLong() {
        assertThat(response.addInt(1L).toString(), is(":1\r\n"));
    }

    @Test
    public void testAddIntBooleanTrue() {
        assertThat(response.addInt(true).toString(), is(":1\r\n"));
    }

    @Test
    public void testAddIntBooleanFalse() {
        assertThat(response.addInt(false).toString(), is(":0\r\n"));
    }

    @Test
    public void testAddError() {
        assertThat(response.addError("ERROR").toString(), is("-ERROR\r\n"));
    }

    @Test
    public void testAddArrayNull() {
        assertThat(response.addArray(null).toString(), is("*0\r\n"));
    }

}
