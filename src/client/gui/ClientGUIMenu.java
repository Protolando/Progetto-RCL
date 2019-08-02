package client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class ClientGUIMenu implements ClientGUIElement {

  private final ResourceBundle stringsBundle;
  private JButton edit;
  private JButton show;
  private JButton showWhole;
  private JButton update;
  private JButton logout;
  private JList<String> filesList;
  private JTextField section;
  private JPanel panel;

  public ClientGUIMenu(ArrayList<String> DocumentNames) {
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
    c.gridwidth = 2;
    c.insets = new Insets(20, 20, 20, 20);
    c.fill = GridBagConstraints.BOTH;
    JScrollPane scroll = new JScrollPane(filesList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    gridBag.setConstraints(scroll, c);
    panel.add(scroll);
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(10, 10, 10, 10);

    JLabel sectionLabel = new JLabel(stringsBundle.getString("Section"));
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.EAST;
    gridBag.setConstraints(sectionLabel, c);
    panel.add(sectionLabel);

    section = new JTextField(10);
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(section, c);
    panel.add(section);

    update = new JButton(stringsBundle.getString("Update"));
    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.EAST;
    gridBag.setConstraints(update, c);
    panel.add(update);

    edit = new JButton(stringsBundle.getString("Edit"));
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(edit, c);
    panel.add(edit);

    show = new JButton(stringsBundle.getString("Show"));
    c.gridx = 0;
    c.gridy = 3;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.EAST;
    gridBag.setConstraints(show, c);
    panel.add(show);

    showWhole = new JButton(stringsBundle.getString("ShowWhole"));
    c.gridx = 1;
    c.gridy = 3;
    c.weightx = 0.1;
    c.weighty = 0.1;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints(showWhole, c);
    panel.add(showWhole);

    logout = new JButton(stringsBundle.getString("Logout"));
    c.gridx = 0;
    c.gridy = 4;
    c.weightx = 0.1;
    c.weighty = 0.1;
    gridBag.setConstraints(logout, c);
    panel.add(logout);
  }

  public JPanel getPanel() {
    return panel;
  }

  public String getWindowTitle() {
    return stringsBundle.getString("WindowName");
  }

  public String getSection() {
    return section.getText();
  }

  public void updateFileList(ArrayList<String> newFileList) {
    DefaultListModel<String> model = new DefaultListModel<>();
    for (String s : newFileList) {
      model.addElement(s);
    }
    filesList.setModel(model);
  }

  public String getSelected() {
    return filesList.getSelectedValue();
  }
}
