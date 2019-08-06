package server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import share.*;

public class RegistrationServer extends UnicastRemoteObject implements TURINGRegister {

  RegistrationServer() throws RemoteException { }

  @Override
  public void register(String username, String password)
      throws IllegalArgumentException, UsernameAlreadyUsedException, IOException {
    System.out.println("New Registration");
    if (username == null || password == null || username.equals("") || password.equals("")) {
      throw new IllegalArgumentException();
    }

    UsersManager.register(username, password);
  }
}
