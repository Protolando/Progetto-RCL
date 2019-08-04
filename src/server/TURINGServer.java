package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import share.Request;
import share.TURINGRegister;
import share.UsernameAlreadyUsedException;
import share.WrongPasswordException;

public class TURINGServer {

  private final static int regPort = 3141;
  private final static int tcpPort = 4562;
  private final static int BUFFER_CAPACITY = 1024;
  private final static int poolSize = 10;

  private HashMap<String, String> registeredUsers;
  private HashSet<LoggedUser> loggedUsers;
  private ObjectMapper mapper;
  private ThreadPoolExecutor threadPool;
  private ByteBuffer buffer;
  private Selector selector;

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
    /*Buffer per la lettura da socket*/
    buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
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
    System.out.println("Accepted connection from " + client);
    client.configureBlocking(false);
    /*Registro il client per la lettura*/
    client.register(selector, SelectionKey.OP_READ);
  }

  private void readFromChannel(SelectionKey key) throws IOException {
    SocketChannel client = (SocketChannel) key.channel();
    buffer.clear();
    /*Leggo tutti i dati inviati e li salvo in una stringa (assumo che le comunicazioni siano sempre stringhe JSON)*/
    StringBuilder builder = new StringBuilder();
    /*Finche` la lettura ha successo*/
    while (client.read(buffer) > 0) {
      buffer.flip();
      /*Svuoto il buffer*/
      builder.append(buffer.asCharBuffer());
      buffer.clear();
    }
    /*Istanzio il messaggio letto in una classe request*/
    Request r = mapper.readValue(builder.toString(), Request.class);
    /*Sottometto il task al thread pool*/
    threadPool.execute(new ServerTask(this, r, key));
  }

  void addToSelector(SelectionKey key, int op) throws ClosedChannelException {
    key.channel().register(selector, op);
  }

  private void writeToChannel(SelectionKey key) throws IOException {
    SocketChannel client = (SocketChannel) key.channel();
    buffer.clear();
    /*Scrivo nel buffer il contenuto dell'attachment di key*/
    buffer.put(mapper.writeValueAsString(key.attachment()).getBytes());
    /*Mando il buffer al client*/
    client.write(buffer);
    buffer.clear();
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
    if(registeredUsers.get(username.toLowerCase()) != null)
      throw new UsernameAlreadyUsedException();
    else {
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
          } if (key.isWritable()) {
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
