package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingWorker;
import share.Request;
import share.RequestType;

public class ClientActionListenerLogin implements ActionListener {

  /*Listener per ClientGUILogin*/

  private final TURINGClient parent;

  ClientActionListenerLogin(TURINGClient client) {
    super();

    parent = client;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    switch (actionEvent.getActionCommand()) {
      case "Register":
        new SwingWorker() {
          /*In un thread diverso da quello dell'interfaccia*/
          @Override
          protected Object doInBackground() {
            parent.sendRegisterRequest();
            return null;
          }
        }.execute();
        break;
      case "Login":
        Request r = new Request(RequestType.LOGIN);
        parent.sendMessage(r);
        break;
    }
  }
}
