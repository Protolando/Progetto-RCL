package client;

import client.gui.ClientGUIHandler;
import client.gui.ClientGUILogin;
import client.gui.ClientGUIMenu;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import share.Request;
import share.ServerErrorException;
import share.UsernameAlreadyUsedException;

public class TURINGClient {

  static final String ServerAddres = "127.0.0.1";
  static final int ServerPort = 4562;
  private final ClientGUIHandler GUI;
  private NetworkInterface networkInterface;
  private ClientGUILogin login;
  private ResourceBundle strings;
  private ClientGUIMenu menu;

  public static void main(String[] args) {
    /*Inizializza GUI*/
    new TURINGClient();
  }

  public TURINGClient() {
    networkInterface = null;
    try {
      networkInterface = new NetworkInterface();
    } catch (IOException e) {
      e.printStackTrace(); /*Server offline TODO SHOW POPUP*/
    }

    ClientActionListenerLogin al = new ClientActionListenerLogin(this);
    login = new ClientGUILogin(al);
    GUI = new ClientGUIHandler(login);
    strings = ResourceBundle.getBundle("client.resources.ClientStrings");
    menu = null;
  }

  public void sendRegisterRequest() {
    if (login == null) {
      return;
    }

    try {
      NetworkInterface.sendRegisterRequest(login.getUsernameField(),
          login.getPasswordField());
      GUI.setUINotices(strings.getString("RegistrationSuccessful"));
    } catch (
        UsernameAlreadyUsedException e) {
      GUI.setUINotices(strings.getString("UsernameUsed"));
    } catch (
        IllegalArgumentException e) {
      GUI.setUINotices(strings.getString("IllegalArgument"));
    } catch (
        ServerErrorException e) {
      GUI.setUINotices(strings.getString("ServerError"));
    }
  }

  public void sendMessageForResult(Request r) {
    /*Inserisco il payload in r*/
    switch (r.getRequestType()) {
      case LOGIN:
        HashMap<String, String> payload = new HashMap<>();
        payload.put("Username", login.getUsernameField());
        payload.put("Password", login.getPasswordField());
        r.setPayload(payload);
        break;
      case LOGOUT:
        break;
      case CREATE:
        break;
      case INVITE:
        break;
      case LIST:
        break;
      case EDIT:
        break;
      case END_EDIT:
        break;
      case SHOW_SECTION:
        break;
      case SHOW_DOCUMENT:
        break;
      case GET_MESSAGES:
        break;
      case SEND_MESSAGE:
        break;
      case SERVER_RESPONSE:
        break;
    }

    /*Faccio partire un thread worker per non eseguire operazioni di rete nel thread dell'interfaccia*/
    new SwingWorker<Request, Object>() {
      @Override
      protected Request doInBackground() throws Exception {
        return networkInterface.sendMessageForResult(r);
      }

      @Override
      protected void done() {
        try {
          update(get());
        } catch (InterruptedException | ExecutionException ignored) {}
      }
    }.execute();
  }

  public void update(Request r) {
    switch (r.getRequestType()) {
      case LOGIN:
        if (r.getPayload().get("Message").equals("OK")) {
          if (menu == null) {
            menu = new ClientGUIMenu(new ArrayList<>());
          }
          GUI.switchPanel(menu);
        } else {
          GUI.setUINotices(r.getPayload().get("Message"));
        }
        break;
      case LOGOUT:
        break;
      case CREATE:
        break;
      case INVITE:
        break;
      case LIST:
        break;
      case EDIT:
        break;
      case END_EDIT:
        break;
      case SHOW_SECTION:
        break;
      case SHOW_DOCUMENT:
        break;
      case GET_MESSAGES:
        break;
      case SEND_MESSAGE:
        break;
      case SERVER_RESPONSE:
        break;
      default:
    }
  }
}
