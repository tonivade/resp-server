package tonivade.redis;

import tonivade.redis.command.CommandSuite;

public class RedisServerMain {

    public static void main(String[] args) {
        RedisServer redisServer = new RedisServer("localhost", 12345, new CommandSuite());

        redisServer.start();
    }

}
