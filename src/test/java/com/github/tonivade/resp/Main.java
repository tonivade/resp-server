package com.github.tonivade.resp;

import com.github.tonivade.resp.command.CommandSuite;

public class Main {
  
  public static void main(String[] args) {
    RedisServer server = new RedisServer("localhost", 7081, new CommandSuite());
    server.start();
  }

}
