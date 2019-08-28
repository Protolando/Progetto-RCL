package client.gui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ClientGUIHandler extends JFrame {
  /*
  * Classe che gestisce la finestra
  */

  public ClientGUIHandler(ClientGUIElement e) {
    /*Schermata iniziale*/
    setSize(800, 600);
    setContentPane(e.getPanel());
    setTitle(e.getWindowTitle());
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public synchronized void switchPanel(ClientGUIElement e) {
    /*Cambia schermata*/
    SwingUtilities.invokeLater(() -> {
      getContentPane().removeAll();
      setContentPane(e.getPanel());
      setTitle(e.getWindowTitle());
      setVisible(true);
    });
  }

  public void showPopup(Object[] message, String title, Object[] options) {
    /*
    * message: corpo del messaggio
    * title: titolo
    * optons: pulsanti
    */
    JOptionPane
        .showOptionDialog(
            this,
            message,
            title,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            null);
  }
}
