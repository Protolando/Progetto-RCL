package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import share.Request;
import share.RequestType;

public class ChatServer extends Thread {
  /*Classe che gestisce l'invio e la ricezione di messagi di chat con UDP Multicast*/

  private static final int BUFFER_SIZE = 1000 * 1000;
  private final String multicastAddress; //Indirizzo del gruppo multicast
  private final TURINGClient turingClient;
  private MulticastSocket ms; //Socket

  ChatServer(String multicastAddress, TURINGClient client) {
    this.multicastAddress = multicastAddress;
    this.turingClient = client;
  }

  void sendChatMessage(String message) throws IOException {
    /*Invia un messaggio di chat all'indirizzo multicast*/

    /*Preparo il buffer*/
    byte[] buffer = (message + "\0").getBytes(StandardCharsets.UTF_8);
    /*Preparo il pacchetto*/
    InetAddress address = InetAddress.getByName(multicastAddress);
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address,
        TURINGClient.CHAT_PORT);

    /*Preparo il socket*/
    DatagramSocket socket = new DatagramSocket();
    /*Invio il pacchetto*/
    socket.send(packet);
  }

  @Override
  public void run() {
    try {
      /*creo il socket*/
      ms = new MulticastSocket(TURINGClient.CHAT_PORT);
      InetAddress ia = InetAddress.getByName(multicastAddress);
      ms.joinGroup(ia); /*Mi unisco al gruppo di multicast*/

      /*Creo un packet UDP*/
      byte[] buffer = new byte[BUFFER_SIZE];
      DatagramPacket dp = new DatagramPacket(buffer, BUFFER_SIZE);

      while (!Thread.currentThread().isInterrupted()) {
        /*Finchè il thread non è stato chiuso sono in attesa di messaggi*/
        ms.receive(dp);
        /*Istanzio Request e la mando al client per aggiornare l'UI*/
        Request r = new Request(RequestType.GET_MESSAGES);
        String msg = new String(dp.getData(), 0, dp.getLength(), StandardCharsets.UTF_8);
        r.putInPayload("Message", msg.substring(0, msg.indexOf("\0")) + "\n");
        turingClient.update(r);
      }
    } catch (IOException ignored) {
    }
  }


  @Override
  public void interrupt() {
    super.interrupt();
    if (ms != null) {
      ms.close();
    }
  }
}

