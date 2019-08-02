package share;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TURINGRegister extends Remote {

  String SERVICE_NAME = "Register";

  void register(String username, String Password)
      throws RemoteException, IllegalArgumentException, UsernameAlreadyUsedException;
}
