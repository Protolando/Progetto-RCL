package client.gui;

import javax.swing.JPanel;

public interface ClientGUIElement {
  JPanel getPanel();
  String getWindowTitle();
  void setUINotices(String s);
}
