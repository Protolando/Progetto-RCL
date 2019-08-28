package client.gui;

import javax.swing.JPanel;

public interface ClientGUIElement {

  /*
   * Interfaccia che definisce un elemento dell'UI. Ad ogni elemento della UI corrisponde un
   * pannello di Java Swing.
   */

  JPanel getPanel();

  String getWindowTitle();
}
