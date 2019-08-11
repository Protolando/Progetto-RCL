
package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import share.Request;
import share.RequestType;

public class ClientActionListenerMenu implements ActionListener {

  private TURINGClient parent;

  public ClientActionListenerMenu(TURINGClient client) {
    super();

    parent = client;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    Request r;
    switch (actionEvent.getActionCommand()) {
      case "Aggiorna Lista":
        r = new Request(RequestType.LIST);
        parent.sendMessageForResult(r);
        break;
      case "Nuovo File":
        r = new Request(RequestType.CREATE);
        parent.sendMessage(r);
        break;
      case "Mostra tutto":
        r = new Request(RequestType.SHOW_DOCUMENT);
        parent.sendMessageForResult(r);
        break;
      case "Disconnetti":
        r = new Request(RequestType.LOGOUT);
        parent.sendMessage(r);
        break;
      case "Invita utente":
        r = new Request(RequestType.INVITE);
        parent.sendMessage(r);
        break;
      case "Modifica Sezione":
        r = new Request(RequestType.EDIT);
        parent.sendMessageForResult(r);
        break;
      case "Mostra sezione":
        r = new Request(RequestType.SHOW_SECTION);
        parent.sendMessageForResult(r);
        break;
    }
  }
}
