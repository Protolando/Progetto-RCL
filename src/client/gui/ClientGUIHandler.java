package client.gui;

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

  public void showPopup(Object[] o){
    JOptionPane.showConfirmDialog(this, o);
  }
}
