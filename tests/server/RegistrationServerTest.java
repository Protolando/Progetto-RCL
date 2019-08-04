package server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import share.UsernameAlreadyUsedException;

class RegistrationServerTest {

  @Test
  void registerNoArguments() {
    TURINGServer server = new TURINGServer();
    try {
      RegistrationServer r = new RegistrationServer(server);
      r.register(null, "");
      fail();
    } catch (IllegalArgumentException ignored) {
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  void registerNewUser() {
    TURINGServer server = new TURINGServer();
    try {
      RegistrationServer r = new RegistrationServer(server);
      r.register("new", "new");
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  void registerUserAgain() {
    TURINGServer server = new TURINGServer();
    RegistrationServer r = null;

    //Registro il primo utente
    try {
      r = new RegistrationServer(server);
      r.register("new", "new");
    } catch (Exception e) {
      fail();
    }

    //Registro il secondo utente
    try {
      r.register("new", "new");
      fail();
    } catch (UsernameAlreadyUsedException ignored) {
    } catch (Exception e) {
      fail();
    }
  }
}