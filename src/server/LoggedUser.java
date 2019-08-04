package server;

import java.nio.channels.SocketChannel;

public class LoggedUser {

  private String username;
  private SocketChannel channel;

  public String getUsername() {
    return username;
  }

  public SocketChannel getSocket() {
    return channel;
  }

  public LoggedUser(String username, SocketChannel socket) {
    this.username = username;
    this.channel = socket;
  }
}
