package com.github.tonivade.resp;

import com.github.tonivade.resp.command.Session;

public interface SessionListener {

  void sessionDeleted(Session session);

  void sessionCreated(Session session);
  
  static SessionListener nullListener() {
    return new SessionListener() {
      @Override
      public void sessionDeleted(Session session) {
        // do nothing
      }
      
      @Override
      public void sessionCreated(Session session) {
        // do nothing
      }
    };
  }
}
