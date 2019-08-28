package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import share.NetworkInterface;
import share.Request;
import share.ServerErrorException;
import share.TURINGRegister;
import share.UsernameAlreadyUsedException;

class NetworkHandler {
  /*
   * Classe che gestisce le comunicazioni di rete ad alto livello.
   * Thread Safe.
   */

  private final SocketChannel connection;
  private final NetworkInterface networkInterface;

  NetworkHandler() throws IOException {
    SocketAddress address = new InetSocketAddress(TURINGClient.ServerAddress,
        TURINGClient.ServerPort);
    /*Apro il socket channel e istanzio la networkInterface*/
    connection = SocketChannel.open(address);
    networkInterface = new NetworkInterface();
  }

  static void sendRegisterRequest(String username, String password)
      throws UsernameAlreadyUsedException, ServerErrorException {
    /*Registrazione tramite RMI*/
    try {
      /*Inizializza registry*/
      Registry r = LocateRegistry.getRegistry(TURINGClient.ServerAddress, 3141);
      /*Ottieni lo stub della classe per registrare*/
      TURINGRegister reg = (TURINGRegister) r.lookup(TURINGRegister.SERVICE_NAME);
      /*Registra*/
      reg.register(username, password);
    } catch (IOException | NotBoundException e) {
      /*Se una di queste eccezioni e` stata sollevata, c'e` un problema con il server */
      throw new ServerErrorException();
    }
  }

  void sendMessage(Request r) throws IOException {
    /*Invia un messaggio sulla connessione*/
    ObjectMapper mapper = new ObjectMapper();
    String msg = mapper.writeValueAsString(r);
    networkInterface.write(connection, msg);
  }

  Request readFromChannel() throws IOException {
    /*Legge dal canale e restituisce una Request*/
    ObjectMapper mapper = new ObjectMapper();

    String read = networkInterface.read(connection);
    if (read == null) {
      return null;
    } else {
      return mapper.readValue(read, Request.class);
    }
  }

  void disconnect() throws IOException {
    /*Disconnette*/
    connection.close();
  }
}
