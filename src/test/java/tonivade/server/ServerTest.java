package tonivade.server;

import tonivade.redis.RedisServer;
import tonivade.redis.command.CommandSuite;

public class ServerTest {

    public static void main(String[] args) {
        RedisServer server = new RedisServer("localhost", 12345, new CommandSuite());

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
    }

}
