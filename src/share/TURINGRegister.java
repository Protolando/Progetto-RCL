package share;

import java.io.IOException;
import java.rmi.Remote;

public interface TURINGRegister extends Remote {

  String SERVICE_NAME = "Register";

  void register(String username, String Password)
      throws IOException, IllegalArgumentException, UsernameAlreadyUsedException;
}
