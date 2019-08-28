package server;

import java.nio.channels.SelectionKey;

public class LoggedUser {
  /*Classe che rappresenta un utente connesso al server*/

  /*Nome dell'utente*/
  private final String username;
  /*Key del selector*/
  private final SelectionKey key;

  LoggedUser(String username, SelectionKey socket) {
    this.username = username;
    this.key = socket;
  }

  public String getUsername() {
    return username;
  }

  SelectionKey getKey() {
    return key;
  }
}
