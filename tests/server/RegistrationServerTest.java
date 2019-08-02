package server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import share.UsernameAlreadyUsedException;

class RegistrationServerTest {

  @Test
  void registerNoArguments() {
    HashMap<String, String> test = new HashMap<>();
    try {
      RegistrationServer r = new RegistrationServer(test);
      r.register(null, "");
      Assertions.fail();
    } catch (IllegalArgumentException ignored) {
    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  void registerNewUser() {
    HashMap<String, String> test = new HashMap<>();
    try {
      RegistrationServer r = new RegistrationServer(test);
      r.register("new", "new");
    } catch (Exception e) {
      Assertions.fail();
    }
  }

  @Test
  void registerUserAgain() {
    HashMap<String, String> test = new HashMap<>();
    RegistrationServer r = null;

    //Registro il primo utente
    try {
      r = new RegistrationServer(test);
      r.register("new", "new");
    } catch (Exception e) {
      Assertions.fail();
    }

    //Registro il secondo utente
    try {
      r.register("new", "new");
      Assertions.fail();
    } catch (UsernameAlreadyUsedException ignored) {
    } catch (Exception e) {
      Assertions.fail();
    }
  }
}