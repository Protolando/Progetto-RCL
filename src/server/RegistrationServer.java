package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import share.*;

public class RegistrationServer extends UnicastRemoteObject implements TURINGRegister {

  private TURINGServer server;

  protected RegistrationServer(TURINGServer server) throws RemoteException {
    super();
    this.server = server;
  }

  @Override
  public void register(String username, String password)
      throws IllegalArgumentException, UsernameAlreadyUsedException {
    System.out.println("New Registration");
    if (username == null || password == null || username.equals("") || password.equals("")) {
      throw new IllegalArgumentException();
    }

    server.register(username, password);
  }
}
