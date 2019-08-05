package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import share.NetworkIntefrace;
import share.Request;
import share.TURINGRegister;
import share.UsernameAlreadyUsedException;
import share.WrongPasswordException;

public class TURINGServer {

  private final static int regPort = 3141;
  private final static int tcpPort = 4562;
  private final static int BUFFER_SIZE = 1024;
  private final static int poolSize = 10;

  private HashMap<String, String> registeredUsers;
  private HashSet<LoggedUser> loggedUsers;
  private ObjectMapper mapper;
  private ThreadPoolExecutor threadPool;
  private Selector selector;
  private NetworkIntefrace networkInterface;

  TURINGServer() {
    /*Lista degli utenti registrati*/
    registeredUsers = new HashMap<>();
    /*TODO leggere la lista da file*/
    /*Hash set che contiene gli utenti connessi*/
    loggedUsers = new HashSet<>();
    /*Inizializzo il JSON mapper*/
    mapper = new ObjectMapper();
    /*Inizializzo il thread pool*/
    threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
    networkInterface = new NetworkIntefrace();
  }

  private void startRMIServer() throws RemoteException {
    /*Inizializzazione del registry per l'RMI (Registrazione)*/
    TURINGRegister stub = new RegistrationServer(this);
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

    /*Istanzio il messaggio letto in una classe request*/
    Request r = mapper.readValue(networkInterface.read(client), Request.class);
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

  boolean isRegistered(String username) {
    return (registeredUsers.get(username.toLowerCase()) != null);
  }

  boolean isLoggedIn(String username) {
    return false;
  }

  void login(String username, String password) throws WrongPasswordException {
  }

  void register(String username, String password) throws UsernameAlreadyUsedException {
    /*Controllo se lo username e` gia` utilizzato*/
    if (registeredUsers.get(username.toLowerCase()) != null) {
      throw new UsernameAlreadyUsedException();
    } else {
      registeredUsers.put(username.toLowerCase(), password);
    }
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
          }
          if (key.isWritable()) {
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
}
