package client.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ClientGUIHandler extends JFrame {

  private ClientGUIElement active;

  public ClientGUIHandler(ClientGUIElement e) {
    active = e;
    setSize(800, 600);
    setContentPane(e.getPanel());
    setTitle(e.getWindowTitle());
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public void switchPanel(ClientGUIElement e) {
    active = e;
    getContentPane().removeAll();
    setContentPane(e.getPanel());
    setTitle(e.getWindowTitle());
    setVisible(true);
  }

  public void setUINotices(String s) {
    /*Eseguo nel thread dell'UI*/
    SwingUtilities.invokeLater(() -> active.setUINotices(s));
  }

  public int showPopup(Object[] message, String title, Object[] options) {
    return JOptionPane
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
