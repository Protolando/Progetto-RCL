package server;

import java.nio.channels.SelectionKey;

public class LoggedUser {

  private String username;
  private SelectionKey key;

  public String getUsername() {
    return username;
  }

  public SelectionKey getKey() {
    return key;
  }

  public LoggedUser(String username, SelectionKey socket) {
    this.username = username;
    this.key = socket;
  }
}
