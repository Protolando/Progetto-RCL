package client.gui;

import client.ClientActionListenerLogin;
import client.TURINGClient;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClientGUILogin implements ClientGUIElement {
  /*Classe che si occupa di mostrare l'interfaccia utente*/

  private ResourceBundle stringsBundle;
  private JLabel UINotices;
  private JTextField passwordField;
  private JFormattedTextField usernameField;
  private JPanel panel;

  public ClientGUILogin(ClientActionListenerLogin al) {
    stringsBundle = ResourceBundle.getBundle("client.resources.ClientStrings");
    panel = new JPanel();

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.CENTER;
    UINotices = new JLabel();
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 2;
    c.insets = new Insets(10, 10, 10, 10);
    gridBag.setConstraints(UINotices, c);
    panel.add(UINotices);
    UINotices.setVisible(false);
    panel.setLayout(gridBag);

    c.gridwidth = 1;
    c.anchor = GridBagConstraints.CENTER;

    JLabel usernameLabel = new JLabel(stringsBundle.getString("Username"));
    c.gridx = 0;
    c.gridy = 0;
    gridBag.setConstraints(usernameLabel, c);
    panel.add(usernameLabel);
    usernameField = TURINGClient.getStringTextField();
    c.gridx = 1;
    c.gridy = 0;
    gridBag.setConstraints(usernameField, c);
    panel.add(usernameField);
    JLabel passwordLabel = new JLabel(stringsBundle.getString("Password"));
    c.gridx = 0;
    c.gridy = 1;
    gridBag.setConstraints(passwordLabel, c);
    panel.add(passwordLabel);
    passwordField = new JPasswordField("", 10);
    c.gridx = 1;
    c.gridy = 1;
    gridBag.setConstraints(passwordField, c);
    panel.add(passwordField);

    JButton login = new JButton(stringsBundle.getString("LoginBtn"));
    c.gridx = 0;
    c.gridy = 2;
    gridBag.setConstraints(login, c);
    panel.add(login);

    JButton register = new JButton(stringsBundle.getString("RegisterBtn"));
    c.gridx = 1;
    c.gridy = 2;
    gridBag.setConstraints(register, c);
    panel.add(register);

    /*Handler dei pulsanti*/
    login.addActionListener(al);
    register.addActionListener(al);
  }

  public JPanel getPanel() {
    return panel;
  }

  public String getWindowTitle() {
    return stringsBundle.getString("WindowName");
  }

  public String getUsernameField() {
    return usernameField.getText();
  }

  public String getPasswordField() {
    return passwordField.getText();
  }

  public void setUINotices(String notice) {
    SwingUtilities.invokeLater(() -> {
          UINotices.setText(notice);
          UINotices.setVisible(true);
        }
    );
  }
}
