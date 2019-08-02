package client.gui;

import javax.swing.JFrame;

public class ClientGUIHandler extends JFrame {

  public ClientGUIHandler(ClientGUIElement e) {
    setSize(800, 600);
    setContentPane(e.getPanel());
    setTitle(e.getWindowTitle());
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public void switchPanel(ClientGUIElement e) {
    getContentPane().removeAll();
    setContentPane(e.getPanel());
    setTitle(e.getWindowTitle());
    setVisible(true);
  }
}
