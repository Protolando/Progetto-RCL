package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

  private final String username;
  private final String password;
  private final String[] invites;

  @JsonCreator
  public User(@JsonProperty("username") String username, @JsonProperty("password") String password,
      @JsonProperty("invites") String[] invites) {
    this.username = username;
    this.password = password;
    this.invites = invites;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String[] getInvites() {
    return invites;
  }
}
