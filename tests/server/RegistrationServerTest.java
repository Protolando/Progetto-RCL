package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import share.UsernameAlreadyUsedException;

class RegistrationServerTest {

  @Test
  void registerNoArguments() {
    try {
      RegistrationServer r = new RegistrationServer();
      r.register(null, "");
      fail();
    } catch (IllegalArgumentException ignored) {
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  void registerNewUser() {
    try {
      RegistrationServer r = new RegistrationServer();
      r.register("new", "new");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  void registerUserAgain() {
    RegistrationServer r = null;
    String username = "new";
    String password = "new";

    //Registro il primo utente
    try {
      r = new RegistrationServer();
      r.register(username, password);
    } catch (Exception e) {
      fail();
    }

    //Registro il secondo utente
    try {
      r.register(username, password);
      fail();
    } catch (UsernameAlreadyUsedException ignored) {
    } catch (Exception e) {
      fail();
    }
  }

  @AfterEach
  void tearDown() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }
}