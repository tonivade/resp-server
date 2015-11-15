package tonivade.redis;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import tonivade.redis.protocol.RedisToken;
import tonivade.redis.protocol.RedisTokenType;

public class RedisClientTest {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static final int TIMEOUT = 1000;

    @Rule
    public RedisServerRule redisServerRule = new RedisServerRule(HOST, PORT);

    private RedisClient redisClient;

    private IRedisCallback callback = mock(IRedisCallback.class);

    @Before
    public void setUp() {
        redisClient = new RedisClient(HOST, PORT, callback);
    }

    @Test
    public void onConnect() {
        redisClient.start();

        verify(callback, timeout(1000)).onConnect();
    }

    @Test
    public void onMessage() {
        redisClient.start();
        verify(callback, timeout(TIMEOUT)).onConnect();

        redisClient.send("PING\r\n");

        ArgumentCaptor<RedisToken> captor = ArgumentCaptor.forClass(RedisToken.class);

        verify(callback, timeout(TIMEOUT)).onMessage(captor.capture());

        RedisToken token = captor.getValue();
        assertThat(token.getType(), equalTo(RedisTokenType.STATUS));
        assertThat(token.<String>getValue(), equalTo("PONG"));
    }

    @Test
    public void onClientDisconnect() {
        redisClient.start();
        verify(callback, timeout(TIMEOUT)).onConnect();

        redisClient.stop();
        verify(callback, timeout(TIMEOUT)).onDisconnect();
    }

    @Test(expected = NullPointerException.class)
    public void requireHost() {
        new RedisClient(null, 0, callback);
    }

    @Test(expected = IllegalArgumentException.class)
    public void requirePortLowerThan1024() {
        new RedisClient("localshot", 0, callback);
    }

    @Test(expected = IllegalArgumentException.class)
    public void requirePortGreaterThan65535() {
        new RedisClient("localshot", 987654321, callback);
    }

    @Test(expected = NullPointerException.class)
    public void requireCallback() {
        new RedisClient("localhost", 12345, null);
    }
}
