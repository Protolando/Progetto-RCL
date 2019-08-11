package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Arrays;

public class User {

  private final String username;
  private final String password;
  private final ArrayList<String> invites;

  public User(String username, String password) {
    this.username = username;
    this.password = password;
    invites = new ArrayList<>();
  }

  @JsonCreator
  public User(@JsonProperty("username") String username, @JsonProperty("password") String password,
      @JsonProperty("invites") String[] invites) {
    this.username = username;
    this.password = password;
    this.invites = new ArrayList<>(Arrays.asList(invites));
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void addInvite(String filePath) {
    invites.add(filePath);
  }

  @JsonProperty("invites")
  public String[] getInvites() {
    return invites.toArray(new String[0]);
  }
}
