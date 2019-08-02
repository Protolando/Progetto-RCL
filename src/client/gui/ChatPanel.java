package client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatPanel {

  private JButton button;
  private JTextArea messages;
  private JPanel panel;
  private JTextField newMsg;

  public ChatPanel() {
    ResourceBundle stringsBundle = ResourceBundle.getBundle("client.resources.ClientStrings");

    panel = new JPanel();
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridBag);
    c.gridx = 0;
    c.weightx = 1;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(5, 5, 5, 5);

    messages = new JTextArea();
    messages.setLineWrap(true);
    messages.setEditable(false);
    JScrollPane scroll = new JScrollPane(messages, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    c.gridy = 0;
    c.weighty = 0.8;
    gridBag.setConstraints(scroll, c);
    panel.add(scroll);

    newMsg = new JTextField(10);
    c.gridy = 1;
    c.weighty = 0.1;
    gridBag.setConstraints(newMsg, c);
    panel.add(newMsg);

    button = new JButton(stringsBundle.getString("ChatSend"));
    c.gridy = 2;
    c.weighty = 0.1;
    gridBag.setConstraints(button, c);
    panel.add(button);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void addMessage(String message) {
    messages.append(message);
  }

  public String getNewMsg() {
    return newMsg.getText();
  }
}
