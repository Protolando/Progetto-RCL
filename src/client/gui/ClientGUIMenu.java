package client.gui;

import client.ClientActionListenerMenu;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ClientGUIMenu implements ClientGUIElement {
  /*Schermata del menu*/

  private final ResourceBundle stringsBundle;
  private final JList<String> filesList;
  private final JPanel panel;

  public ClientGUIMenu(ArrayList<String> DocumentNames, ClientActionListenerMenu al) {
    stringsBundle = ResourceBundle.getBundle("client.resources.ClientStrings");

    panel = new JPanel();

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridBag);
    c.anchor = GridBagConstraints.CENTER;

    filesList = new JList<>(DocumentNames.toArray(new String[0]));
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.8;
    c.weighty = 0.8;
    c.gridwidth = 3;
    c.insets = new Insets(20, 20, 20, 20);
    c.fill = GridBagConstraints.BOTH;
    JScrollPane scroll = new JScrollPane(filesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    gridBag.setConstraints(scroll, c);
    panel.add(scroll);
    c.gridwidth = 1;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(10, 10, 10, 10);
    c.fill = GridBagConstraints.NONE;

    JButton update = new JButton(stringsBundle.getString("Update"));
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(update, c);
    panel.add(update);

    JButton newFile = new JButton(stringsBundle.getString("NewFile"));
    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(newFile, c);
    panel.add(newFile);

    JButton showWhole = new JButton(stringsBundle.getString("ShowWhole"));
    c.gridx = 0;
    c.gridy = 3;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(showWhole, c);
    panel.add(showWhole);

    JButton invite = new JButton(stringsBundle.getString("Invite"));
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(invite, c);
    panel.add(invite);

    JButton edit = new JButton(stringsBundle.getString("Edit"));
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(edit, c);
    panel.add(edit);

    JButton show = new JButton(stringsBundle.getString("Show"));
    c.gridx = 1;
    c.gridy = 3;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(show, c);
    panel.add(show);

    JButton logout = new JButton(stringsBundle.getString("Logout"));
    c.gridx = 0;
    c.gridy = 4;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(logout, c);
    panel.add(logout);

    edit.addActionListener(al);
    show.addActionListener(al);
    showWhole.addActionListener(al);
    update.addActionListener(al);
    logout.addActionListener(al);
    newFile.addActionListener(al);
    invite.addActionListener(al);
  }

  public JPanel getPanel() {
    return panel;
  }

  public String getWindowTitle() {
    return stringsBundle.getString("WindowName");
  }

  public void updateFileList(ArrayList<String> newFileList) {
    synchronized (filesList) {
      DefaultListModel<String> model = new DefaultListModel<>();
      for (String s : newFileList) {
        model.addElement(s);
      }
      filesList.setModel(model);
    }
  }

  public String getSelected() {
    return filesList.getSelectedValue();
  }
}
