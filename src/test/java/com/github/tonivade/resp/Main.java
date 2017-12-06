package com.github.tonivade.resp;

import com.github.tonivade.resp.command.CommandSuite;

public class Main {
  
  public static void main(String[] args) {
    RespServer server = new RespServer(new RespServerContext("localhost", 7081, new CommandSuite()));
    server.start();
  }

}
