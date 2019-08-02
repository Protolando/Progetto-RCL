package client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ClientGUIDocument implements ClientGUIElement {

  private final ResourceBundle stringsBundle;
  private JButton commit;
  private JButton quit;
  private JTextArea document;
  private ChatPanel chat;
  private String filename;
  private JPanel panel;

  public ClientGUIDocument(String filename, String fileText) {
    stringsBundle = ResourceBundle.getBundle("client.resources.ClientStrings");

    this.filename = filename;
    panel = new JPanel();

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridBag);
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(10, 10, 10, 10);

    document = new JTextArea(fileText);
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

    commit = new JButton(stringsBundle.getString("CommitEdits"));
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.2;
    c.weighty = 0.2;
    c.anchor = GridBagConstraints.CENTER;
    gridBag.setConstraints(commit, c);
    panel.add(commit);

    quit = new JButton(stringsBundle.getString("Quit"));
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.2;
    c.weighty = 0.2;
    c.anchor = GridBagConstraints.CENTER;
    gridBag.setConstraints(quit, c);
    panel.add(quit);
    c.anchor = GridBagConstraints.NONE;

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
  }

  public String getDocument() {
    return document.getText();
  }

  public String getNewMessageContent() {
    return chat.getNewMsg();
  }

  public void updateChat(ArrayList<String> messages) {
    for (String s : messages) {
      chat.addMessage(s);
    }
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
