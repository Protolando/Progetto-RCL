package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import share.Request;
import share.RequestType;

public class ClientActionListenerDocument implements ActionListener {

  private TURINGClient server;

  public ClientActionListenerDocument(TURINGClient server) {
    this.server = server;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    Request r = null;
    switch (actionEvent.getActionCommand()) {
      case "Salva":
        r = new Request(RequestType.END_EDIT);
        break;
      /*todo*/
      case "Esci":
        r = new Request(RequestType.END_EDIT);
        break;
      case "Invia":
        server.sendChatMessage();
        break;
    }

    if (!actionEvent.getActionCommand().equals("Invia")) {
      server.sendMessage(r);
    }
  }
}
