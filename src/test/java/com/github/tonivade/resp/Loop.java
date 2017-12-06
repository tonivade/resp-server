package com.github.tonivade.resp;

import com.github.tonivade.resp.command.CommandSuite;

public class Loop {

  public static void main(String[] args) throws Exception {
    RespServer server = new RespServer(new RespServerContext("localhost", 7081, new CommandSuite()));

    for (int i = 0; i < 1000; i++) {
      server.start();
      Thread.sleep(1000);

      server.stop();
      Thread.sleep(1000);
    }
  }
}
