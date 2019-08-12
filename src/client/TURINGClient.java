package client;

import client.gui.ClientGUIDocument;
import client.gui.ClientGUIHandler;
import client.gui.ClientGUILogin;
import client.gui.ClientGUIMenu;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.NumberFormatter;
import share.Request;
import share.RequestType;
import share.ServerErrorException;
import share.UsernameAlreadyUsedException;

public class TURINGClient {

  static final String ServerAddress = "127.0.0.1";
  static final int ServerPort = 4562;
  static final int CHAT_PORT = 5969;
  private final ClientGUIHandler GUI;
  private NetworkHandler networkHandler;
  private ResourceBundle strings;
  private ClientGUILogin login;
  private ClientGUIMenu menu;
  private ClientGUIDocument document;
  private ChatServer chatServer;

  public static void main(String[] args) {
    /*Inizializza GUI*/
    new TURINGClient();
  }

  public TURINGClient() {
    ClientActionListenerLogin al = new ClientActionListenerLogin(this);
    login = new ClientGUILogin(al);
    GUI = new ClientGUIHandler(login);
    strings = ResourceBundle.getBundle("client.resources.ClientStrings");
    menu = null;
    document = null;
  }

  public void connectToNetwork() {
    boolean success = false;
    while (!success) {
      try {
        networkHandler = new NetworkHandler();
        success = true;
      } catch (IOException e) {
        GUI.showPopup(new Object[]{"Connessione fallita"},
            "Connessione fallita", new String[]{"Riprovare"});
      }
    }
  }

  public void sendRegisterRequest() {
    if (login == null) {
      return;
    }

    if (login.getUsernameField().equals("") || login.getPasswordField().equals("")) {
      GUI.showPopup(new Object[]{"Compilare i campi"}, "Errore", new Object[]{"OK"});
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
    boolean error = false;

    /*Inserisco il payload in r*/
    switch (r.getRequestType()) {
      case LOGIN:
        if (login.getUsernameField().equals("") || login.getPasswordField().equals("")) {
          GUI.showPopup(new Object[]{"Compilare i campi"}, "Errore", new Object[]{"OK"});
          error = true;
          break;
        }
        r.putInPayload("Username", login.getUsernameField());
        r.putInPayload("Password", login.getPasswordField());
        break;
      case EDIT:
      case SHOW_SECTION:
        JFormattedTextField sectionNum = getNumberTextField();
        GUI.showPopup(new Object[]{"Sezione", sectionNum}, "Sezione", new Object[]{"OK"});
        if (sectionNum.getText().equals("")) {
          GUI.showPopup(new Object[]{"Inserire un numero di sezione"}, "Errore",
              new Object[]{"OK"});
          error = true;
          break;
        }

        r.putInPayload("nSection", sectionNum.getText());
      case SHOW_DOCUMENT:
        if (menu.getSelected() == null) {
          GUI.showPopup(new Object[]{"Selezionare un documento"}, "Errore", new Object[]{"OK"});
          error = true;
          break;
        }

        r.putInPayload("filename", menu.getSelected());
        break;
      case LIST:
      case SERVER_RESPONSE:
        break;
    }

    if (!error) {
      /*Faccio partire un thread worker per non eseguire operazioni di rete nel thread dell'interfaccia*/
      new SwingWorker<Request, Object>() {
        @Override
        protected Request doInBackground() throws Exception {
          if (r.getRequestType() == RequestType.LOGIN) {
            connectToNetwork();
          }

          Request reply;

          try {
            reply = networkHandler.sendMessageForResult(r);
          } catch (IOException e) {
            try {
              networkHandler.disconnect();
            } catch (IOException ignored) {
            }
            disconnected();
            return null;
          }
          return reply;
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
  }

  public void sendMessage(Request r) {
    boolean error = false;

    switch (r.getRequestType()) {
      case LOGOUT:
        login = new ClientGUILogin(new ClientActionListenerLogin(this));
        GUI.switchPanel(login);
        break;
      case CREATE:
        JFormattedTextField fileName = getStringTextField();
        JFormattedTextField nSections = getNumberTextField();
        GUI.showPopup(new Object[]{
                "Crea File",
                "Nome File: ", fileName,
                "Numero Sezioni:", nSections
            },
            "Crea File",
            new Object[]{"OK"}
        );
        if (fileName.getText().equals("") || nSections.getText().equals("")) {
          GUI.showPopup(new Object[]{"Parametri errati"}, "Errore", new Object[]{"OK"});
          error = true;
          break;
        }

        r.putInPayload("Filename", fileName.getText());
        r.putInPayload("nSections", nSections.getText());
        break;
      case INVITE:
        if (menu.getSelected() == null) {
          GUI.showPopup(new Object[]{"Selezionare un documento"}, "Errore", new Object[]{"OK"});
          error = true;
          break;
        }

        JFormattedTextField username = getStringTextField();
        GUI.showPopup(new Object[]{
                "Scegliere l'utente da invitare",
                "Nome Utente: ", username
            },
            "Username",
            new Object[]{"OK"}
        );

        if (username.getText().equals("")) {
          GUI.showPopup(new Object[]{"Parametri errati"}, "Errore", new Object[]{"OK"});
          error = true;
          break;
        }
        r.putInPayload("username", username.getText());
        r.putInPayload("filename", menu.getSelected());
        break;
      case END_EDIT:
        r.putInPayload("document", document.getDocument());
      case CANCEL_EDIT:
        chatServer.interrupt();
        chatServer = null;
        menu = new ClientGUIMenu(new ArrayList<>(), new ClientActionListenerMenu(this));
        GUI.switchPanel(menu);
        break;
    }

    if (!error) {
      new SwingWorker() {
        @Override
        protected Object doInBackground() {
          try {
            networkHandler.sendMessage(r);
          } catch (IOException e) {
            try {
              networkHandler.disconnect();
            } catch (IOException ignored) {
            }
            disconnected();
          }

          if (r.getRequestType() == RequestType.LOGOUT) {
            try {
              networkHandler.disconnect();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          return null;
        }
      }.execute();

      if (r.getRequestType() == RequestType.END_EDIT
          || r.getRequestType() == RequestType.CANCEL_EDIT) {
        sendMessageForResult(new Request(RequestType.LIST));
      }
    }
  }

  private void disconnected() {
    GUI.showPopup(new Object[]{"Sei stato disconnesso dal server"}, "Errore", new Object[]{"OK"});
    login = new ClientGUILogin(new ClientActionListenerLogin(this));
    GUI.switchPanel(login);
  }

  public static JFormattedTextField getNumberTextField() {
    NumberFormat format = NumberFormat.getInstance();
    format.setGroupingUsed(false);
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setValueClass(Integer.class);
    formatter.setAllowsInvalid(false);
    return new JFormattedTextField(formatter);
  }

  public static JFormattedTextField getStringTextField() {
    DefaultFormatter formatter = new DefaultFormatter();
    formatter.setValueClass(String.class);
    formatter.setAllowsInvalid(false);
    JFormattedTextField textField = new JFormattedTextField();
    textField.setColumns(10);

    return textField;
  }

  public void sendChatMessage() {
    new SwingWorker() {
      @Override
      protected Object doInBackground() {

        boolean sent = false;
        int timeout = 0;

        while (!sent && timeout < 10) {
          try {
            chatServer.sendChatMessage(document.getNewMessageContent());
            sent = true;
          } catch (IOException e) {
            /*Lo rifaccio finche` fino a 10 volte se fallisce*/
            timeout++;
          }
        }
        return null;
      }
    }.execute();
  }

  public void update(Request r) {
    switch (r.getRequestType()) {
      case LOGIN:
        if (r.getPayload().get("Message").equals("OK")) {
          menu = new ClientGUIMenu(new ArrayList<>(), new ClientActionListenerMenu(this));
          GUI.switchPanel(menu);
          sendMessageForResult(new Request(RequestType.LIST));
        } else {
          GUI.setUINotices(r.getPayload().get("Message"));
        }
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
        if (r.getPayload().get("Message") != null) {
          GUI.showPopup(new Object[]{"Errore: " + r.getPayload().get("Message")}, "Errore",
              new Object[]{"OK"});
          break;
        }
        document = new ClientGUIDocument(menu.getSelected(),
            r.getPayload().get("file"), true, new ClientActionListenerDocument(this));
        GUI.switchPanel(document);
        chatServer = new ChatServer(r.getPayload().get("MulticastAddress"), this);
        chatServer.start();
        break;
      case SHOW_SECTION:
      case SHOW_DOCUMENT:
        if (r.getPayload().get("Message") != null) {
          GUI.showPopup(new Object[]{"Errore: " + r.getPayload().get("Message")}, "Errore",
              new Object[]{"OK"});
          break;
        }

        document = new ClientGUIDocument(menu.getSelected(),
            r.getPayload().get("file"), false, new ClientActionListenerDocument(this));
        GUI.switchPanel(document);
        break;
      case GET_MESSAGES:
        SwingUtilities.invokeLater(() -> {
          document.updateChat(r.getPayload().get("Message"));
          //GUI.update();
        });
        break;
      case SERVER_RESPONSE:
        break;
      default:
    }
  }
}
