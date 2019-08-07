package client;

import client.gui.ClientGUIHandler;
import client.gui.ClientGUILogin;
import client.gui.ClientGUIMenu;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import share.Request;
import share.RequestType;
import share.ServerErrorException;
import share.UsernameAlreadyUsedException;

public class TURINGClient {

  static final String ServerAddress = "127.0.0.1";
  static final int ServerPort = 4562;
  private final ClientGUIHandler GUI;
  private NetworkHandler networkHandler;
  private ClientGUILogin login;
  private ResourceBundle strings;
  private ClientGUIMenu menu;

  public static void main(String[] args) {
    /*Inizializza GUI*/
    new TURINGClient();
  }

  public TURINGClient() {
    networkHandler = null;
    try {
      networkHandler = new NetworkHandler();
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
      NetworkHandler.sendRegisterRequest(login.getUsernameField(),
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
        r.putInPayload("Username", login.getUsernameField());
        r.putInPayload("Password", login.getPasswordField());
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
        return networkHandler.sendMessageForResult(r);
      }

      @Override
      protected void done() {
        try {
          update(get());
        } catch (InterruptedException | ExecutionException ignored) {
        }
      }
    }.execute();
  }

  public void sendMessage(Request r) {
    switch (r.getRequestType()) {
      case LOGOUT:
        login = new ClientGUILogin(new ClientActionListenerLogin(this));
        GUI.switchPanel(login);
        /*TODO close connection*/
        break;
      case CREATE:
        JTextField fileName = new JTextField();
        JTextField nSections = new JTextField();
        GUI.showPopup(new Object[]{
            "Nome File: ", fileName,
            "Numero Sezioni:", nSections
        });
        r.putInPayload("Filename", fileName.getText());
        r.putInPayload("nSections", nSections.getText());
        break;
    }

    new SwingWorker() {
      @Override
      protected Object doInBackground() throws IOException {
        networkHandler.sendMessage(r);
        return null;
      }
    }.execute();
  }

  public void update(Request r) {
    switch (r.getRequestType()) {
      case LOGIN:
        if (r.getPayload().get("Message").equals("OK")) {
          if (menu == null) {
            menu = new ClientGUIMenu(new ArrayList<>(), new ClientActionListenerMenu(this));
          }
          GUI.switchPanel(menu);
          sendMessageForResult(new Request(RequestType.LIST));
        } else {
          GUI.setUINotices(r.getPayload().get("Message"));
        }
        break;
      case INVITE:
        break;
      case LIST:
        int listElems = Integer.parseInt(r.getPayload().get("nfiles"));
        ArrayList<String> listaFiles = new ArrayList<>();
        for (int i = 0; i < listElems; i++) {
          listaFiles.add(r.getPayload().get(String.valueOf(i)));
        }
        SwingUtilities.invokeLater(() -> menu.updateFileList(listaFiles));
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
