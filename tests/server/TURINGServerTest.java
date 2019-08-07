package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import share.UserNotFoundException;
import share.UsernameAlreadyUsedException;
import share.WrongPasswordException;

class TURINGServerTest {

  @BeforeEach
  void setUp() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }

  @Test
  void failLogin() {
    String username = "test";
    String password = "test";
    TURINGServer server = new TURINGServer();
    try {
      server.login(username, password, null);
      fail();
    } catch (WrongPasswordException e) {
      fail();
    } catch (UserNotFoundException ignored) {
    }
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }
    try {
      server.login(username, "wrongpassword", null);
      fail();
    } catch (WrongPasswordException ignored) {
    } catch (UserNotFoundException e) {
      fail();
    }
  }

  @Test
  void successLogin() {
    String username = "test";
    String password = "test";
    TURINGServer server = new TURINGServer();
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }
    try {
      server.login(username, password, null);
    } catch (WrongPasswordException | UserNotFoundException e) {
      fail();
    }
  }

  @Test
  void isLoggedIn() {
    String username = "test";
    String password = "test";
    TURINGServer server = new TURINGServer();
    assertFalse(server.isLoggedIn(username));
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }
    assertFalse(server.isLoggedIn(username));
    try {
      server.login(username, password, null);
    } catch (WrongPasswordException | UserNotFoundException e) {
      fail();
    }
    assertTrue(server.isLoggedIn(username));
  }

  @AfterEach
  void tearDown() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }

  @Test
  void logout() {
    String username = "test";
    String password = "test";
    TURINGServer server = new TURINGServer();
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }
    try {
      server.login(username, password, null);
    } catch (WrongPasswordException | UserNotFoundException e) {
      fail();
    }
    assertTrue(server.isLoggedIn(username));
    server.logout(username);
    assertFalse(server.isLoggedIn(username));
  }
}