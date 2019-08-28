package client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ClientGUIDocument implements ClientGUIElement {
  /*Classe che implementa l'interfaccia grafica per visualizzare o modificare i documenti*/

  private final JTextArea document;
  private ChatPanel chat;
  private final String filename;
  private final JPanel panel;
  private final boolean editable;

  public ClientGUIDocument(String filename, String fileText, boolean editable,
      ActionListener al) {
    /*
     * filename = nome del file. filetext = contenuto del file
     * Editable == true SSE posso modificare il documento (Edit section)
     * Editable == false SSE non posso (Show Section/Show Document)
     */
    ResourceBundle stringsBundle = ResourceBundle.getBundle("client.resources.ClientStrings");
    this.editable = editable;
    this.filename = filename;

    /*Inizializzo l'UI*/
    panel = new JPanel();
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridBag);
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(10, 10, 10, 10);

    document = new JTextArea(fileText);
    document.setEditable(editable);
    document.setLineWrap(true);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 0.8;
    c.weighty = 0.8;
    c.gridwidth = 2;
    JScrollPane scroll = new JScrollPane(document, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    gridBag.setConstraints(scroll, c);
    panel.add(scroll);
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;

    if (editable) {
      JButton commit = new JButton(stringsBundle.getString("CommitEdits"));
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0.2;
      c.weighty = 0.2;
      c.anchor = GridBagConstraints.CENTER;
      gridBag.setConstraints(commit, c);
      panel.add(commit);

      chat = new ChatPanel();
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 2;
      c.gridy = 0;
      c.weightx = 0.3;
      c.weighty = 1;
      c.anchor = GridBagConstraints.EAST;
      c.gridheight = GridBagConstraints.REMAINDER;
      gridBag.setConstraints(chat.getPanel(), c);
      panel.add(chat.getPanel());
      chat.addSendActionListener(al);
      c.gridheight = GridBagConstraints.NONE;
      c.fill = GridBagConstraints.NONE;

      commit.addActionListener(al);
    }

    JButton quit = new JButton(stringsBundle.getString("Quit"));
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.2;
    c.weighty = 0.2;
    c.anchor = GridBagConstraints.CENTER;
    gridBag.setConstraints(quit, c);
    panel.add(quit);
    c.anchor = GridBagConstraints.NONE;

    quit.addActionListener(al);
  }

  public String getDocument() {
    return document.getText();
  }

  public String getNewMessageContent() {
    if (chat != null) {
      return chat.getNewMsg();
    } else {
      return null;
    }
  }

  public void updateChat(String message) {
    if (chat != null) {
      chat.addMessage(message);
    }
  }

  public boolean isEditable() {
    return editable;
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public String getWindowTitle() {
    return filename;
  }
}
