package server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import share.UsernameAlreadyUsedException;

class TURINGServerTest {

  @Test
  void isRegistered() {
    TURINGServer server = new TURINGServer();
    try {
      server.register("test", "test");
    } catch (UsernameAlreadyUsedException e) {
      fail();
    }
    assertTrue(server.isRegistered("test"));
  }

  @Test
  void isRegistered2() {
    TURINGServer server = new TURINGServer();
    assertFalse(server.isRegistered("test"));
  }

  @Test
  void isLoggedIn() {
  }

  @Test
  void login() {
  }
}