package server;

import static java.lang.Math.floor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import share.NetworkInterface;
import share.Request;
import share.TURINGRegister;
import share.UserNotFoundException;
import share.WrongPasswordException;

public class TURINGServer {
  /*
   * Classe che si occupa dell'interfaccia tra network e logica dell'applicaione
   */

  /*Porta per RMI*/
  private final static int regPort = 3141;
  /*Porta per TCP*/
  private final static int tcpPort = 4562;
  /*Dimensione del thread pool*/
  private final static int poolSize = 10;
  /*Map che contiene la lista degli utenti connessi (chiave: username)*/
  private final ConcurrentHashMap<String, LoggedUser> loggedUsers;
  /*Mapper per esportare JSON*/
  private final ObjectMapper mapper;
  /*Executor di ServerTasks*/
  private final ThreadPoolExecutor threadPool;
  /*Selector delle connessioni sul socket*/
  private Selector selector;
  /*Per lettura e scrittura dal socket*/
  private final NetworkInterface networkInterface;
  /*Gestore del filesystem*/
  private final FilesManager filesManager;
  /*Per il generatore di indirizzi multicast*/
  private int multicastAddressCounter;

  public static void main(String[] args) {
    /*Eseguibile del server*/
    System.out.println("Server partito");

    TURINGServer server = new TURINGServer();
    try {
      /*Inizializzo server socket e RMI*/
      server.startRMIServer();
      server.startSocketServer();
    } catch (IOException e) {
      /*Eccezione non recuperabile*/
      e.printStackTrace();
      return;
    }

    /*Finche` il server e` aperto*/
    while (true) {
      Iterator<SelectionKey> iterator;

      try {
        /*Prendo le chiavi selezionate dall'iteratore*/
        iterator = server.getSelectedKeys().iterator();
      } catch (IOException e) {
        e.printStackTrace();
        break;
      }

      while (iterator.hasNext()) {
        /*Per ogni chiave selezionata*/
        SelectionKey key = iterator.next();
        iterator.remove();
        synchronized (key) {
          /*Faccio quello per cui la chiave è stata selezionata*/
          try {
            if (key.isAcceptable()) {
              server.acceptNewConnection(key);
            } else if (key.isReadable()) {
              server.readFromChannel(key);
            } else if (key.isWritable()) {
              server.writeToChannel(key);
            }
          } catch (IOException ex) {
            key.cancel();
            try {
              key.channel().close();
            } catch (IOException e) {
              e.printStackTrace();
              return;
            }
          }
        }
      }
    }

    /*Server fermato*/
    System.out.println("Server Chiuso");
  }

  TURINGServer() {
    /*Hash set che contiene gli utenti connessi*/
    loggedUsers = new ConcurrentHashMap<>();
    /*Inizializzo il JSON mapper*/
    mapper = new ObjectMapper();
    /*Inizializzo il thread pool*/
    threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
    networkInterface = new NetworkInterface();
    filesManager = new FilesManager();
    multicastAddressCounter = 0;
  }

  private void startRMIServer() throws RemoteException {
    /*Inizializzazione del registry per l'RMI (Registrazione)*/
    TURINGRegister stub = new RegistrationServer();
    LocateRegistry.createRegistry(regPort);
    Registry r = LocateRegistry.getRegistry(regPort);
    r.rebind(TURINGRegister.SERVICE_NAME, stub);
    System.out.println("Registration Server pronto.");
  }

  /************************ Gestione comunicazione sul socket ************************/

  private void startSocketServer() throws IOException {
    /*Inizializzo il selector sulla porta tcpPort*/
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    ServerSocket ss = serverChannel.socket();
    InetSocketAddress address = new InetSocketAddress(tcpPort);
    ss.bind(address);
    serverChannel.configureBlocking(false);
    selector = Selector.open();
    /*Di default voglio essere notificato di tutte le richieste di connessione*/
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    System.out.println("ServerChannel pronto.");
  }

  private Set<SelectionKey> getSelectedKeys() throws IOException {
    /*Prendo la lista dei socket pronti*/
    selector.select();
    return selector.selectedKeys();
  }

  private void acceptNewConnection(SelectionKey key) throws IOException {
    /*Se la key e` accettabile accetto la connessione e registro il canale per la lettura*/
    ServerSocketChannel socket = (ServerSocketChannel) key.channel();
    SocketChannel client = socket.accept();
    System.out.println("Accettata connessione da " + client);
    client.configureBlocking(false);
    /*Registro il client per la lettura*/
    client.register(selector, SelectionKey.OP_READ);
  }

  private void readFromChannel(SelectionKey key) throws IOException {
    /*Leggo dal canale*/
    SocketChannel client = (SocketChannel) key.channel();
    String msg = networkInterface.read(client);

    /*se non ho ricevuto un messaggio esco*/
    if (msg == null) {
      return;
    }

    /*Istanzio il messaggio letto in una classe request*/
    Request r = mapper.readValue(msg, Request.class);
    /*Sottometto il task al thread pool*/
    threadPool.execute(new ServerTask(this, r, key));
  }

  private void writeToChannel(SelectionKey key) throws IOException {
    /*Scrivo sul canale*/
    SocketChannel client = (SocketChannel) key.channel();
    /*passo a networkInterface il contenuto dell'attachment di key*/
    ArrayList<String> messages = (ArrayList<String>) key.attachment();

    for (String message : messages) {
      networkInterface.write(client, message);
    }

    key.attach(null);
    /*Smetto di seguire il canale per la scrittura*/
    removeInterestFromKey(key, SelectionKey.OP_WRITE);
  }

  void addInterestToKey(SelectionKey key, int op) {
    /*Aggiunge op all'interest di key*/
    synchronized (key) {
      key.interestOps(key.interestOps() | op);
    }
    selector.wakeup();
  }

  private void removeInterestFromKey(SelectionKey key, int op) {
    /*Rimuove op dall'interest di key*/
    synchronized (key) {
      key.interestOps(key.interestOps() & ~op);
    }
  }

  /************************ Gestione Utenti ************************/

  void login(String username, String password, SelectionKey key)
      throws WrongPasswordException, UserNotFoundException, IllegalArgumentException {
    /*Effettua il login di un utente*/
    if (username == null || username.equals("") || password == null || password.equals("")) {
      throw new IllegalArgumentException();
    }

    if (!UsersManager.isRegistered(username)) {
      /*Utente non registrato*/
      throw new UserNotFoundException();
    } else {
      User u;
      /*Recupero i file dell'utente*/
      u = UsersManager.getUserData(username);
      if (u.getPassword().equals(password)) {
        /*Controllo la password, se è corretta inserisco l'utente in loggedUsers*/
        loggedUsers.put(username, new LoggedUser(username, key));
      } else {
        /*Altrimenti sollevo un'eccezione*/
        throw new WrongPasswordException();
      }
    }
  }

  void logout(String username) {
    /*Effettua il logout di un utente*/

    if (loggedUsers.get(username).getKey() != null) {
      /*Se la chiave è diversa da null, devo chiudere il canale e cancellare la chiave*/
      try {
        loggedUsers.get(username).getKey().channel().close();
      } catch (IOException ignored) {
      }
      /*Cancello la chiave*/
      loggedUsers.get(username).getKey().cancel();
    }

    /*Segno che l'utente non sta più modificando nessun file*/
    FileSection userOpenSection = filesManager.getOpenEdits(username);
    if (userOpenSection != null) {
      removeFromEditing(userOpenSection.getOpenBy());
    }

    /*Rimuovo l'utente*/
    loggedUsers.remove(username);
  }

  boolean isLoggedIn(String username) {
    /*Restituisce true se l'utente è connesso */

    if (loggedUsers.get(username) != null && loggedUsers.get(username).getKey() != null
        && !loggedUsers.get(username).getKey().isValid()) {
      /*In questo caso l'utente risulta connesso, ma ha una chiave invalida, quindi è stato
      disconnesso in maniera anomala. Effettuo il logout*/
      logout(username);
    }

    return loggedUsers.get(username) != null;
  }

  LoggedUser getUserFromKey(SelectionKey key) {
    /*Restituisce l'utente corrispondente ad una chiave*/

    for (String username : loggedUsers.keySet()) {
      if (key.equals(loggedUsers.get(username).getKey())) {
        return loggedUsers.get(username);
      }
    }
    return null;
  }

  SelectionKey getKeyFromUsername(String username) {
    for (LoggedUser u : loggedUsers.values()) {
      if (u.getUsername().equals(username)) {
        return u.getKey();
      }
    }

    return null;
  }

  /************************ Gestione File ************************/

  ServerFile addToEditing(String filename, String owner, int nSection, String editor) {
    /*Aggiunge la sezione del file passato come parametro alla lista dei file aperti in modifica*/

    if (filesManager.getFile(filename, owner) == null) {
      /*se il filesManager non contiene il file, lo aggiungo*/
      filesManager.addFile(new ServerFile(filename, owner, getMulticastAddress()));
    }

    /*Segno che la sezione è aperta in scrittura*/
    ServerFile file = filesManager.getFile(filename, owner);
    file.addOpenSection(new FileSection(nSection, editor, file));
    return file;
  }

  void removeFromEditing(String username) {
    /*Rimuove dalla lista dei file in modifica quello aperto dall'utente passato come parametro*/

    FileSection section = filesManager.getOpenEdits(username);
    if (section == null) {
      /*l'utente non sta modificando nulla*/
      return;
    }

    /*Recupero il file*/
    String filename = section.getFileName();
    String owner = section.getFile().getOwner();
    /*Tolgo la sezione da quelle aperte*/
    filesManager.getFile(filename, owner).removeSection(section.getNSection());

    /*Se non ci sono sezioni aperte cancello il file*/
    if (filesManager.getFile(filename, owner).nSectionOpen() == 0) {
      filesManager.remove(filename, owner);
    }
  }

  boolean canBeEdited(String filename, String owner, int nSection) {
    /*restituisce true se nessuno ha aperto il file in scrittura*/

    if (filesManager.getFile(filename, owner) == null) {
      return true;
    } else {
      return !filesManager.getFile(filename, owner).isInUse(nSection);
    }
  }

  void writeFile(String username, String document) throws IOException {
    /*Scrive su disco il file aperto in modifica da username*/

    /*Recupero il file che l'utente sta modificando*/
    FileSection section = filesManager.getOpenEdits(username);
    /*Lo scrivo su disco*/
    UsersManager.writeToFile(section, document);
  }

  /************************ Altro ************************/

  private synchronized String getMulticastAddress() {
    /*Restituisce un indirizzo di multicast nuovo*/

    String address = "239.255." + (int) floor(multicastAddressCounter / 255) + "." +
        ((multicastAddressCounter % 255) + 1);
    multicastAddressCounter++;

    if (multicastAddressCounter >= 255 * 255) {
      /*Ho usato tutti gli indirizzi, ricomincio da 0. Non è un sistema perfetto ma funziona
      per numeri di utenti relativamente piccoli*/
      multicastAddressCounter = 0;
    }

    return address;
  }
}
