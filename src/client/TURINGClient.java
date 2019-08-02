package client;

import client.gui.ClientGUIHandler;
import client.gui.ClientGUILogin;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;
import share.ServerErrorException;
import share.TURINGRegister;
import share.UsernameAlreadyUsedException;

public class TURINGClient {

  static final String ServerAddres = "127.0.0.1";
  static final int ServerPort = 4562;

  /*TODO
   *  Initialize listeners
   *  DO STUFF!
   */

  public static void main(String[] args) {
    /*Inizializza GUI*/
    ResourceBundle strings = ResourceBundle.getBundle("client.resources.ClientStrings");
    ClientGUILogin login = new ClientGUILogin();
    /*Handler del pulsante registra*/
    login.setRegisterListener(actionEvent -> {
      try {
        sendRegisterRequest(login.getUsernameField(), login.getPasswordField());
        login.setUINotices(strings.getString("RegistrationSuccessful"));
      } catch (UsernameAlreadyUsedException e) {
        login.setUINotices(strings.getString("UsernameUsed"));
      } catch (IllegalArgumentException e) {
        login.setUINotices(strings.getString("IllegalArgument"));
      } catch (ServerErrorException e) {
        login.setUINotices(strings.getString("ServerError"));
      }
    });

    ClientGUIHandler GUI = new ClientGUIHandler(login);


  }

  private static void sendRegisterRequest(String username, String password)
      throws UsernameAlreadyUsedException, ServerErrorException {
    try {
      /*Inizializza registry*/
      Registry r = LocateRegistry.getRegistry(ServerAddres, 3141);
      /*Ottieni lo stub della classe per registrare*/
      TURINGRegister reg = (TURINGRegister) r.lookup(TURINGRegister.SERVICE_NAME);
      /*Registra*/
      reg.register(username, password);
    } catch (RemoteException | NotBoundException e) {
      /*Se una di queste eccezioni e` stata sollevata, c'e` un problema con il server */
      throw new ServerErrorException();
    }
  }
}
