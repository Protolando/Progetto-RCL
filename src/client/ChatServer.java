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

  private static final int BUFFER_SIZE = 1000 * 1000;
  private String multicastAddress;
  private TURINGClient turingClient;
  private MulticastSocket ms;

  public ChatServer(String multicastAddress, TURINGClient client) {
    this.multicastAddress = multicastAddress;
    this.turingClient = client;
  }

  public void sendChatMessage(String message) throws IOException {
    byte[] buffer = (message + "\0").getBytes(StandardCharsets.UTF_8);
    InetAddress address = InetAddress.getByName(multicastAddress);
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address,
        TURINGClient.CHAT_PORT);

    DatagramSocket socket = new DatagramSocket();
    socket.send(packet);
  }

  @Override
  public void run() {
    try {
      ms = new MulticastSocket(TURINGClient.CHAT_PORT);
      InetAddress ia = InetAddress.getByName(multicastAddress);
      ms.joinGroup(ia);

      byte[] buffer = new byte[BUFFER_SIZE];
      DatagramPacket dp = new DatagramPacket(buffer, BUFFER_SIZE);

      while (!Thread.currentThread().isInterrupted()) {
        ms.receive(dp);
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

