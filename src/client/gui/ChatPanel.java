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
import javax.swing.JTextField;

class ChatPanel {
  /*Componente grafico che implementa la chat*/

  private final JButton button;
  private final JTextArea messages;
  private final JPanel panel;
  private final JTextField newMsg;

  ChatPanel() {
    ResourceBundle stringsBundle = ResourceBundle.getBundle("client.resources.ClientStrings");

    /*Creo e posiziono i componenti*/
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

  JPanel getPanel() {
    return panel;
  }

  void addMessage(String message) {
    synchronized (messages) {
      messages.append(message);
    }
  }

  String getNewMsg() {
    synchronized (newMsg) {
      return newMsg.getText();
    }
  }

  void addSendActionListener(ActionListener al) {
    button.addActionListener(al);
  }
}
