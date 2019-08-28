package client;

import java.io.IOException;
import share.Request;

public class ChannelReader extends Thread {

  private final TURINGClient client;

  ChannelReader(TURINGClient client) {
    this.client = client;
  }


  @Override
  public void run() {
    while (!Thread.interrupted()) {
      Request r = null;
      try {
        r = client.readFromChannel();
      } catch (IOException e) {
        /*Se c'è stato un fallimento nella connessione provo a disconnettermi e mostro un
          messaggio di errore all'utente*/
        client.disconnect();
        this.interrupt();
      }
      if (r != null) {
        client.update(r);
      }
    }
  }
}