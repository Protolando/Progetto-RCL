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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import share.NetworkInterface;
import share.Request;
import share.TURINGRegister;
import share.UserNotFoundException;
import share.WrongPasswordException;

public class TURINGServer {

  private final static int regPort = 3141;
  private final static int tcpPort = 4562;
  private final static int poolSize = 10;

  private HashMap<String, LoggedUser> loggedUsers;
  private ObjectMapper mapper;
  private ThreadPoolExecutor threadPool;
  private Selector selector;
  private NetworkInterface networkInterface;
  private FilesManager filesManager;
  private int multicastAddressCounter;

  TURINGServer() {
    /*Hash set che contiene gli utenti connessi*/
    loggedUsers = new HashMap<>();
    /*Inizializzo il JSON mapper*/
    mapper = new ObjectMapper();
    /*Inizializzo il thread pool*/
    threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
    networkInterface = new NetworkInterface();
    filesManager = new FilesManager();
    multicastAddressCounter = 0;
  }

  public ServerFile addToEditing(String filename, String owner, int nSection, String editor) {
    if (filesManager.getFile(filename, owner) == null) {
      filesManager.addFile(new ServerFile(filename, owner, getMulticastAddress()));
    }

    ServerFile file = filesManager.getFile(filename, owner);
    file.addOpenSection(new FileSection(nSection, editor, file));
    return file;
  }

  private String getMulticastAddress() {
    String address = "239.255." + (int) floor(multicastAddressCounter / 255) + "." +
        String.valueOf((multicastAddressCounter % 255) + 1);
    multicastAddressCounter++;
    return address;
  }

  public void removeFromEditing(String username) {
    FileSection section = filesManager.getOpenEdits(username);
    String filename = section.getFileName();
    String owner = section.getFile().getOwner();
    filesManager.getFile(filename, owner).removeSection(section.getNSection());

    if (filesManager.getFile(filename, owner).nSectionOpen() == 0) {
      filesManager.remove(filename, owner);
    }
  }

  public boolean canBeEdited(String filename, String owner, int nSection) {
    if (filesManager.getFile(filename, owner) == null) {
      return true;
    } else {
      return !filesManager.getFile(filename, owner).isInUse(nSection);
    }
  }

  private void startRMIServer() throws RemoteException {
    /*Inizializzazione del registry per l'RMI (Registrazione)*/
    TURINGRegister stub = new RegistrationServer();
    LocateRegistry.createRegistry(regPort);
    Registry r = LocateRegistry.getRegistry(regPort);
    r.rebind(TURINGRegister.SERVICE_NAME, stub);
    System.out.println("Registration Server pronto.");
  }

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
    SocketChannel client = (SocketChannel) key.channel();

    String reply = networkInterface.read(client);

    if (reply == null) {
      return;
    }

    /*Istanzio il messaggio letto in una classe request*/
    Request r = mapper.readValue(reply, Request.class);
    /*Sottometto il task al thread pool*/
    threadPool.execute(new ServerTask(this, r, key));
  }

  void addInterestToKey(SelectionKey key, int op) {
    key.interestOps(key.interestOps() | op);
    selector.wakeup();
  }

  void removeInterestFromKey(SelectionKey key, int op) {
    key.interestOps(key.interestOps() & ~op);
  }

  private void writeToChannel(SelectionKey key) throws IOException {
    SocketChannel client = (SocketChannel) key.channel();
    /*Scrivo nel buffer il contenuto dell'attachment di key*/
    String message = (String) key.attachment();

    networkInterface.write(client, message);

    /*Rimuovo il channel dagli scrivibili*/
    removeInterestFromKey(key, SelectionKey.OP_WRITE);
  }

  boolean isLoggedIn(String username) {
    if (loggedUsers.get(username) != null && loggedUsers.get(username).getKey() != null
        && !loggedUsers.get(username).getKey().isValid()) {
      logout(username);
    }
    return loggedUsers.get(username) != null;
  }

  void logout(String username) {
    if (loggedUsers.get(username).getKey() != null) {
      try {
        loggedUsers.get(username).getKey().channel().close();
      } catch (IOException ignored) {
      }
      /*Cancello la chiave*/
      loggedUsers.get(username).getKey().cancel();
    }
    FileSection userOpenSection = filesManager.getOpenEdits(username);
    if (userOpenSection != null) {
      removeFromEditing(userOpenSection.getOpenBy());
    }
    /*Chiudo il canale se necessario*/
    loggedUsers.remove(username);
  }

  void login(String username, String password, SelectionKey key)
      throws WrongPasswordException, UserNotFoundException, IllegalArgumentException {
    if (username == null || username.equals("") || password == null || password.equals("")) {
      throw new IllegalArgumentException();
    }

    if (!UsersManager.isRegistered(username)) {
      throw new UserNotFoundException();
    } else {
      User u;
      try {
        u = UsersManager.getUserData(username);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      if (u.getPassword().equals(password)) {
        loggedUsers.put(username, new LoggedUser(username, key));
      } else {
        throw new WrongPasswordException();
      }
    }
  }

  public LoggedUser getUserFromKey(SelectionKey key) {
    for (String username : loggedUsers.keySet()) {
      if (key.equals(loggedUsers.get(username).getKey())) {
        return loggedUsers.get(username);
      }
    }
    return null;
  }

  public static void main(String[] args) {
    System.out.println("Server partito");

    TURINGServer server = new TURINGServer();
    try {
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
        iterator = server.getSelectedKeys().iterator();
      } catch (IOException e) {
        e.printStackTrace();
        break;
      }

      while (iterator.hasNext()) {
        SelectionKey key = iterator.next();
        iterator.remove();
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

    /*Server fermato*/
  }

  public void writeFile(String username, String document) throws IOException {
    FileSection section = filesManager.getOpenEdits(username);
    UsersManager.writeToFile(section, document);
  }
}
