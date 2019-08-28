
package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import share.Request;
import share.RequestType;

public class ClientActionListenerMenu implements ActionListener {

  /*Listener per ClientGUIMenu*/

  private final TURINGClient parent;

  ClientActionListenerMenu(TURINGClient client) {
    super();

    parent = client;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    Request r = null;
    switch (actionEvent.getActionCommand()) {
      case "Aggiorna Lista":
        r = new Request(RequestType.LIST);
        break;
      case "Nuovo File":
        r = new Request(RequestType.CREATE);
        break;
      case "Mostra tutto":
        r = new Request(RequestType.SHOW_DOCUMENT);
        break;
      case "Disconnetti":
        r = new Request(RequestType.LOGOUT);
        break;
      case "Invita utente":
        r = new Request(RequestType.INVITE);
        break;
      case "Modifica Sezione":
        r = new Request(RequestType.EDIT);
        break;
      case "Mostra sezione":
        r = new Request(RequestType.SHOW_SECTION);
        break;
    }

    if (r != null) {
      parent.sendMessage(r);
    }
  }
}
