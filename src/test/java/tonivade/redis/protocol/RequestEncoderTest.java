package tonivade.redis.protocol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.Charset;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class RequestEncoderTest {

    private Charset utf8 = Charset.forName("UTF-8");

    private RequestEncoder encoder = new RequestEncoder();

    private RedisToken intToken = RedisToken.integer(1);
    private RedisToken abcString = RedisToken.string("abc");
    private RedisToken pongString = RedisToken.status("pong");
    private RedisToken errorString = RedisToken.error("ERR");
    private RedisToken arrayToken = RedisToken.array(intToken, abcString);
    private RedisToken arrayOfArraysToken = RedisToken.array(arrayToken, arrayToken);

    private ByteBuf out = mock(ByteBuf.class);
    private ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

    private ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);

    @Test
    public void encodeString() throws Exception {
        encoder.encode(ctx, abcString, out);

        verify(out).writeBytes(captor.capture());

        assertThat(captor.getValue(), equalTo("$3\r\nabc\r\n".getBytes(utf8)));

    }

    @Test
    public void encodeStatus() throws Exception {
        encoder.encode(ctx, pongString, out);

        verify(out).writeBytes(captor.capture());

        assertThat(captor.getValue(), equalTo("+pong\r\n".getBytes(utf8)));
    }

    @Test
    public void encodeInteger() throws Exception {
        encoder.encode(ctx, intToken, out);

        verify(out).writeBytes(captor.capture());

        assertThat(captor.getValue(), equalTo(":1\r\n".getBytes(utf8)));
    }

    @Test
    public void encodeError() throws Exception {
        encoder.encode(ctx, errorString, out);

        verify(out).writeBytes(captor.capture());

        assertThat(captor.getValue(), equalTo("-ERR\r\n".getBytes(utf8)));
    }

    @Test
    public void encodeArray() throws Exception {
        encoder.encode(ctx, arrayToken, out);

        verify(out).writeBytes(captor.capture());

        assertThat(captor.getValue(), equalTo("*2\r\n:1\r\n$3\r\nabc\r\n".getBytes(utf8)));
    }

    @Test
    public void encodeArrayOfArrays() throws Exception {
        encoder.encode(ctx, arrayOfArraysToken, out);

        verify(out).writeBytes(captor.capture());

        assertThat(captor.getValue(), equalTo("*2\r\n*2\r\n:1\r\n$3\r\nabc\r\n*2\r\n:1\r\n$3\r\nabc\r\n".getBytes(utf8)));
    }

}
