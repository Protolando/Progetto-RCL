package client;

import client.gui.ClientGUIDocument;
import client.gui.ClientGUIHandler;
import client.gui.ClientGUILogin;
import client.gui.ClientGUIMenu;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
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
  /*
   * Classe principale del client. Include il main e interfaccia GUI e comunicazioni di rete.
   */

  /*Indirizzo del server*/
  static final String ServerAddress = "127.0.0.1";
  /*Porta su cui è in ascolto il server per le comunicazioni TCP*/
  static final int ServerPort = 4562;
  /*Porta su cui viaggiano i messaggi di chat*/
  static final int CHAT_PORT = 5969;
  /*Finestra di Java Swing*/
  private final ClientGUIHandler GUI;
  /*Gestore della connessione*/
  private NetworkHandler networkHandler;
  /*Stringhe*/
  private final ResourceBundle strings;
  /*Schermata di login*/
  private ClientGUILogin login;
  /*Schermata del menù*/
  private ClientGUIMenu menu;
  /*Schermata di visualizzazione/modifica del documento*/
  private ClientGUIDocument document;
  /*Server di chat*/
  private ChatServer chatServer;
  /*Nome dell'utente connesso*/
  private String username;
  /*Gestisce la lettura dei messaggi dal socket*/
  private ChannelReader channelReader;
  /*Flag*/
  private boolean connected;

  public static void main(String[] args) {
    /*Fai partire il client. Da quì in poi il programma risponde alle azioni dell'interfaccia
    grafica e ai messaggi che arrivano dalla rete*/
    new TURINGClient();
  }

  private TURINGClient() {
    /*Istanzio la schermata di login e faccio partire l'interfaccia grafica*/
    ClientActionListenerLogin al = new ClientActionListenerLogin(this);
    login = new ClientGUILogin(al);
    GUI = new ClientGUIHandler(login);
    menu = null;
    document = null;
    /*Inizializzo le risorse*/
    strings = ResourceBundle.getBundle("client.resources.ClientStrings");
    connected = false;
  }

  private void connectToServer() {
    /*Prova ad aprire la connessione TCP con il server*/
    while (!connected) {
      try {
        /*Se riesce ad istanziare un NetworkHandler allora la connessione TCP è stata aperta,
        altrimenti è fallita, mostra un messaggio di errore e riprova*/
        networkHandler = new NetworkHandler();
        channelReader = new ChannelReader(this);
        channelReader.start();
        connected = true;
      } catch (IOException e) {
        String connectionFailed = strings.getString("ConnectionFailed");
        String tryAgain = strings.getString("tryAgain");
        GUI.showPopup(new Object[]{connectionFailed},
            connectionFailed, new String[]{tryAgain});
      }
    }
  }

  void sendRegisterRequest() {
    /*Metodo per inviare una richiesta di registrazione*/
    if (login == null) { //Non posso inviare una richiesta se non sono nella schermata di login
      return;
    }

    if (login.getUsernameField().equals("") || login.getPasswordField().equals("")) {
      //I campi sono stati lasciati vuoti, mostro un errore ed esco
      GUI.showPopup(new Object[]{"Compilare i campi"}, "Errore", new Object[]{"OK"});
      return;
    }

    try {
      /*Provo a connettermi e in caso di errore mostro i messaggi corrispondenti*/
      NetworkHandler.sendRegisterRequest(login.getUsernameField(),
          login.getPasswordField());
      GUI.showPopup(new Object[]{strings.getString("RegistrationSuccessful")}, "Errore",
          new Object[]{"OK"});
    } catch (
        UsernameAlreadyUsedException e) {
      GUI.showPopup(new Object[]{strings.getString("UsernameUsed")}, "Errore",
          new Object[]{"OK"});
    } catch (
        IllegalArgumentException e) {
      GUI.showPopup(new Object[]{strings.getString("IllegalArgument")}, "Errore",
          new Object[]{"OK"});
    } catch (
        ServerErrorException e) {
      GUI.showPopup(new Object[]{strings.getString("ServerError")}, "Errore",
          new Object[]{"OK"});
    }
  }

  void sendMessage(Request r) {/*Invio un messaggio e non mi aspetto nessuna risposta*/
    boolean error = false;

    switch (r.getRequestType()) {
      case LOGOUT: //Fai il logout dal server
        break;
      case CREATE: //Crea un nuovo documento
        /*Mostro un popup in cui chiedo il nome del file e il numero di sezioni*/
        JFormattedTextField fileName = getStringTextField();
        JFormattedTextField nSections = getNumberTextField();
        GUI.showPopup(new Object[]{
                strings.getString("CreateFile"),
                strings.getString("FileName"), fileName,
                strings.getString("NumberOfSections"), nSections
            },
            strings.getString("CreateFile"),
            new Object[]{"OK"}
        );
        /*Se sono lasciati vuoti mostro un errore*/
        if (fileName.getText().equals("") || nSections.getText().equals("")) {
          GUI.showPopup(new Object[]{strings.getString("WrongParameters")}, "Errore",
              new Object[]{"OK"});
          error = true;
          break;
        }

        r.putInPayload("Filename", fileName.getText());
        r.putInPayload("nSections", nSections.getText());
        break;
      case INVITE: //Invita un utente a modificare un documento
        if (menu.getSelected() == null) {
          /*Nessun documento selezionato*/
          GUI.showPopup(new Object[]{strings.getString("selectADocument")}, "Errore",
              new Object[]{"OK"});
          error = true;
          break;
        }

        /*Mostro un popup in cui chiedo il nome utente*/
        JFormattedTextField username = getStringTextField();
        GUI.showPopup(new Object[]{
                strings.getString("insertUser"),
                strings.getString("Username"), username
            },
            strings.getString("Username"),
            new Object[]{"OK"}
        );

        /*Se l'username è stato lasciato vuoto errore*/
        if (username.getText().equals("")) {
          GUI.showPopup(new Object[]{strings.getString("WrongParameters")}, "Errore",
              new Object[]{"OK"});
          error = true;
          break;
        }
        r.putInPayload("username", username.getText());
        r.putInPayload("filename", menu.getSelected());
        break;
      case END_EDIT: //Commit di una modifica
        r.putInPayload("document", document.getDocument());
        /*No break, quindi va avanti*/
      case CANCEL_EDIT: //Smetti di modificare una sezione senza salvare le modifiche
        if (document.isEditable()) {
          /*Chiudo la chat*/
          chatServer.interrupt();
          chatServer = null;
        }
        /*Torno al menù di scelta dei documenti*/
        menu = new ClientGUIMenu(new ArrayList<>(), new ClientActionListenerMenu(this));
        GUI.switchPanel(menu);
        break;
      case LOGIN: /*Richiesta di login*/
        if (login.getUsernameField().equals("") || login.getPasswordField().equals("")) {
          /*Campi lasciati vuoti*/
          GUI.showPopup(new Object[]{"Compilare i campi"}, "Errore", new Object[]{"OK"});
          error = true;
          break;
        }
        this.username = login.getUsernameField();
        r.putInPayload("Username", login.getUsernameField());
        r.putInPayload("Password", login.getPasswordField());
        break;
      case EDIT: /*Richiesta di modifica*/
      case SHOW_SECTION: /*Richiesta di visualizzazione di una sezione*/
        /*Mostro popup in cui chiedo il numero di sezione che l'utente vuole modificare/vedere*/
        JFormattedTextField sectionNum = getNumberTextField();
        String section = strings.getString("Section");
        GUI.showPopup(new Object[]{section, sectionNum}, section, new Object[]{"OK"});
        if (sectionNum.getText().equals("")) {
          String insertNum = strings.getString("insertSectionNum");
          GUI.showPopup(new Object[]{insertNum}, "Errore",
              new Object[]{"OK"});
          error = true;
          break;
        }

        r.putInPayload("nSection", sectionNum.getText());
        /*Non c'è break, quindi continua sotto*/
      case SHOW_DOCUMENT: /*Richiesta di visualizzazione del documento completo*/
        if (menu.getSelected() == null) {
          String selectDocument = strings.getString("selectADocument");
          GUI.showPopup(new Object[]{selectDocument}, "Errore", new Object[]{"OK"});
          error = true;
          break;
        }

        r.putInPayload("filename", menu.getSelected());
        break;
      case LIST: /*Richiesta di visualizzazione della lista di file*/
        /*Questo campo non richiede payload*/
        break;
      default:
        /*Se il caso non è nello switch, mostro un errore*/
        GUI.showPopup(new Object[]{strings.getString("UnexpectedError")}, "Errore",
            new Object[]{"OK"});
        error = true;
        break;
    }

    if (!error) {
      /*se non ci sono stati errori faccio inviare il messaggio ad uno swing worker (thread non UI)*/
      new SwingWorker() {
        @Override
        protected Object doInBackground() {

          if (r.getRequestType() == RequestType.LOGIN) {
            /*Se sto facendo il login devo prima connettermi (Inizializzazione lazy)*/
            connectToServer();
          }

          try {
            /*Invio il messaggio al server*/
            networkHandler.sendMessage(r);
          } catch (IOException e) {
            disconnect();
          }
          return null;
        }
      }.execute();

      if (r.getRequestType() == RequestType.END_EDIT
          || r.getRequestType() == RequestType.CANCEL_EDIT) {
        /*Se la richiesta era di modifica, una volta uscito richiedo di
         aggiornare la lista dei documenti nel menu*/
        sendMessage(new Request(RequestType.LIST));
      }
    }
  }

  private static JFormattedTextField getNumberTextField() {
    /*Restituisce un campo di testo che accetta solo numeri come input*/
    NumberFormat format = NumberFormat.getInstance();
    format.setGroupingUsed(false);
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setValueClass(Integer.class);
    formatter.setAllowsInvalid(false);
    return new JFormattedTextField(formatter);
  }

  public static JFormattedTextField getStringTextField() {
    /*Restituisce un campo di testo che accetta solo caratteri come input*/
    DefaultFormatter formatter = new DefaultFormatter();
    formatter.setValueClass(String.class);
    formatter.setAllowsInvalid(false);
    JFormattedTextField textField = new JFormattedTextField();
    textField.setColumns(10);

    return textField;
  }

  void sendChatMessage() {
    /*Invia un messaggio di chat*/
    new SwingWorker() {
      @Override
      protected Object doInBackground() {
        /*In un worker per non lavorare sul thread dell'interfaccia grafica*/
        try {
          chatServer.sendChatMessage(username + ": " + document.getNewMessageContent());
        } catch (IOException e) {
          /*Se fallisce mostro un messaggio di errore*/
          GUI.showPopup(new Object[]{strings.getString("failSendingMessage")}, "Errore",
              new Object[]{"OK"});
        }
        return null;
      }
    }.execute();
  }

  void update(Request r) {
    /*Aggiorna l'interfaccia grafica in seguito ad una risposta dal server*/
    switch (r.getRequestType()) {
      case LOGIN:
        if (r.getPayload().get("Message")
            .equals("OK")) {//login riuscito, passo alla schermata del menu
          menu = new ClientGUIMenu(new ArrayList<>(), new ClientActionListenerMenu(this));
          GUI.switchPanel(menu);
          /*Richiedo la lista dei miei file*/
          sendMessage(new Request(RequestType.LIST));
        } else {
          /*Mostro l'errore restituito dal server se non ho ricevuto OK*/
          GUI.showPopup(new Object[]{r.getPayload().get("Message")}, "Errore",
              new Object[]{"OK"});
        }
        break;
      case LOGOUT:
        disconnect();
      case LIST:
        /*il payload contiene una stringa nfiles che mi dice quanti elementi ci sono da leggere*/
        int listElems = Integer.parseInt(r.getPayload().get("nfiles"));
        /*leggo tutti gli elementi e li metto in un'array list*/
        ArrayList<String> listaFiles = new ArrayList<>();
        for (int i = 0; i < listElems; i++) {
          listaFiles.add(r.getPayload().get(String.valueOf(i)));
        }
        /*Faccio aggiornare l'interfaccia al thread dell'UI*/
        SwingUtilities.invokeLater(() -> menu.updateFileList(listaFiles));
        break;
      case EDIT:
        if (r.getPayload().get("Message") != null) {
          /*se il campo non è null ho ricevuto un errore, lo mostro ed esco*/
          GUI.showPopup(new Object[]{"Errore: " + r.getPayload().get("Message")}, "Errore",
              new Object[]{"OK"});
          break;
        }
        /*Creo la schermata di modifica del documento*/
        document = new ClientGUIDocument(menu.getSelected(),
            r.getPayload().get("file"), true, new ClientActionListenerDocument(this));
        GUI.switchPanel(document);
        /*Inizializzo il server di chat*/
        chatServer = new ChatServer(r.getPayload().get("MulticastAddress"), this);
        chatServer.start();
        break;
      case SHOW_SECTION:
      case SHOW_DOCUMENT:
        if (r.getPayload().get("Message") != null) {
          /*se il campo non è null ho ricevuto un errore, lo mostro ed esco*/
          GUI.showPopup(new Object[]{"Errore: " + r.getPayload().get("Message")}, "Errore",
              new Object[]{"OK"});
          break;
        }

        /*La schermata di visualizzazione del documento è uguale a quella di modifica,
        ma le modifiche sono bloccate*/
        document = new ClientGUIDocument(menu.getSelected(),
            r.getPayload().get("file"), false, new ClientActionListenerDocument(this));
        GUI.switchPanel(document);
        break;
      case GET_MESSAGES:
        /*è arrivato un messaggio di chat, aggiorno l'UI nel thread dell'UI*/
        SwingUtilities.invokeLater(() -> document.updateChat(r.getPayload().get("Message")));
        break;
      case CREATE:
        /*Chiedo un aggiornamento della lista dei file*/
        sendMessage(new Request(RequestType.LIST));
      case INVITE:
      case END_EDIT:
      case CANCEL_EDIT:
      case SERVER_RESPONSE:
        /*Se c'è stato un errore lo mostro*/
        if (r.getPayload().get("Message") != null) {
          GUI.showPopup(new Object[]{"Errore: " + r.getPayload().get("Message")}, "Errore",
              new Object[]{"OK"});
        }
        break;
    }
  }

  Request readFromChannel() throws IOException {
    if (networkHandler == null) {
      return null;
    } else {
      return networkHandler.readFromChannel();
    }
  }

  void disconnect() {
    if (!connected) {
      /*Se non sono già connesso ho finito*/
      return;
    }

    new SwingWorker() {
      @Override
      protected Object doInBackground() {
        try {
          /*Operazione di networking sul thread worker*/
          networkHandler.disconnect();
          networkHandler = null;
        } catch (IOException ignored) {
        }
        return null;
      }
    }.execute();

    if (channelReader != null) {
      /*Chiudo il thread che ascolta la porta tcp*/
      channelReader.interrupt();
      channelReader = null;
    }

    /*Mostra un popup di errore e torna al menu di login se la connessione con il server termina
    in maniera inaspettata*/
    GUI.showPopup(new Object[]{strings.getString("youWereDisconnected")}, "Errore",
        new Object[]{"OK"});
    connected = false;

    /*Cambio la schermata a quella di login*/
    login = new ClientGUILogin(new ClientActionListenerLogin(this));
    GUI.switchPanel(login);
  }
}
