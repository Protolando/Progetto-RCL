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

public class NetworkHandler {

  private SocketChannel connection;
  private NetworkInterface networkInterface;

  public NetworkHandler() throws IOException {
    SocketAddress address = new InetSocketAddress(TURINGClient.ServerAddress,
        TURINGClient.ServerPort);
    connection = SocketChannel.open(address);
    networkInterface = new NetworkInterface();
  }

  public Request sendMessageForResult(Request request) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String msg = mapper.writeValueAsString(request);
    networkInterface.write(connection, msg);

    return mapper.readValue(networkInterface.read(connection), Request.class);
  }

  public static void sendRegisterRequest(String username, String password)
      throws UsernameAlreadyUsedException, ServerErrorException {
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

  public void sendMessage(Request r) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String msg = mapper.writeValueAsString(r);
    networkInterface.write(connection, msg);
  }
}
