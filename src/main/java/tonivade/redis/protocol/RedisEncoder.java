package tonivade.redis.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RedisEncoder extends MessageToByteEncoder<RedisToken> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RedisToken msg, ByteBuf out) throws Exception {
        out.writeBytes(new RedisSerializer().encodeToken(msg));
    }

}