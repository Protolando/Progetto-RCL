package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import share.*;

public class RegistrationServer extends UnicastRemoteObject implements TURINGRegister {
  private HashMap<String, String> users;

  protected RegistrationServer(HashMap<String, String> users) throws RemoteException {
    super();
    this.users = users;
  }

  @Override
  public void register(String Username, String Password)
      throws RemoteException, IllegalArgumentException, UsernameAlreadyUsedException {
    System.out.println("New Registration");
    if(Username == null || Password == null || Username.equals("") || Password.equals(""))
      throw new IllegalArgumentException();

    /*Controllo se lo username e` gia` utilizzato*/
    if(users.get(Username.toLowerCase()) != null)
      throw new UsernameAlreadyUsedException();
    else {
      users.put(Username.toLowerCase(), Password);
    }
  }
}
