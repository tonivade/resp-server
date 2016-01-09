package tonivade.redis.protocol;

import static java.util.stream.Collectors.toList;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import tonivade.redis.command.Response;

public class RequestEncoder extends MessageToByteEncoder<RedisToken> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RedisToken msg, ByteBuf out) throws Exception {
        Response response = new Response();
        switch(msg.getType()) {
        case STRING:
            response.addBulkStr(msg.<SafeString>getValue());
            break;
        case STATUS:
            response.addSimpleStr(msg.<String>getValue());
            break;
        case INTEGER:
            response.addInt(msg.<Integer>getValue());
            break;
        case ERROR:
            response.addError(msg.<String>getValue());
            break;
        case ARRAY:
            response.addArray(collectValues(msg));
            break;
        case UNKNOWN:
            break;
        }
        out.writeBytes(response.getBytes());
    }

    private List<?> collectValues(RedisToken msg) {
        return msg.<List<RedisToken>>getValue().stream().map(RedisToken::getValue).collect(toList());
    }

}
