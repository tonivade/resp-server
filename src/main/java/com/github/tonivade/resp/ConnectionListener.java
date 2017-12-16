package com.github.tonivade.resp;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

class ConnectionListener implements ChannelFutureListener {
  private final RespClient client;
  
  ConnectionListener(RespClient client) {
    this.client = client;
  }
  
  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    if (!future.isSuccess()) {
      EventLoop eventLoop = future.channel().eventLoop();
      eventLoop.schedule(client::start, 1L, TimeUnit.SECONDS); 
    }
  }
}
