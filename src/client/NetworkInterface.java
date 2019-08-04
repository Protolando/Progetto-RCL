package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import share.Request;
import share.ServerErrorException;
import share.TURINGRegister;
import share.UsernameAlreadyUsedException;

public class NetworkInterface {

  private Socket connection;
  private BufferedWriter out;
  private BufferedReader in;

  public NetworkInterface() throws IOException {
    connection = new Socket(TURINGClient.ServerAddres, TURINGClient.ServerPort);
    in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
  }

  public Request sendMessageForResult(Request request) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String message = mapper.writeValueAsString(request);
    out.write(message  + "\n");
    out.flush();
    String res = in.readLine();

    return mapper.readValue(res, Request.class);
  }

  public static void sendRegisterRequest(String username, String password)
      throws UsernameAlreadyUsedException, ServerErrorException {
    try {
      /*Inizializza registry*/
      Registry r = LocateRegistry.getRegistry(TURINGClient.ServerAddres, 3141);
      /*Ottieni lo stub della classe per registrare*/
      TURINGRegister reg = (TURINGRegister) r.lookup(TURINGRegister.SERVICE_NAME);
      /*Registra*/
      reg.register(username, password);
    } catch (RemoteException | NotBoundException e) {
      /*Se una di queste eccezioni e` stata sollevata, c'e` un problema con il server */
      throw new ServerErrorException();
    }
  }
}
