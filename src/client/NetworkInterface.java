package client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import share.Request;
import share.RequestType;

public class NetworkInterface {
  private Socket connection;

  public NetworkInterface() throws IOException {
    connection = new Socket(TURINGClient.ServerAddres, TURINGClient.ServerPort);
  }

  public void sendMessage(NetworkClient client, Request request) throws IOException{
    ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
    out.writeObject(request);
    out.flush();
  }

  public static void main(String[] args) {
    try {
      NetworkInterface ni = new NetworkInterface();
      Request r = new Request(RequestType.LOGIN);
      r.setPayload("Test, Test");
      ni.sendMessage(null, r);
    } catch (IOException e){
      e.printStackTrace();
    }
  }
}
