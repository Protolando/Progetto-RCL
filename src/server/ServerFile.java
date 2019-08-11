package server;

import java.util.HashMap;

public class ServerFile {

  private HashMap<Integer, FileSection> openSections;
  private String owner;
  private String filename;
  private String multicastAddress;

  public ServerFile(String filename, String owner, String multicastAddress) {
    this.openSections = new HashMap<>();
    this.owner = owner;
    this.filename = filename;
    this.multicastAddress = multicastAddress;
  }

  public String getOwner() {
    return owner;
  }

  public String getMulticastAddress() {
    return multicastAddress;
  }

  public void addOpenSection(FileSection section) {
    openSections.put(section.getNSection(), section);
  }

  public FileSection[] getOpenSections() {
    return openSections.values().toArray(new FileSection[0]);
  }

  public boolean isInUse(int sectionNumber) {
    return (openSections.get(sectionNumber) != null);
  }

  public String getFilename() {
    return filename;
  }

  public int nSectionOpen() {
    return getOpenSections().length;
  }

  public void removeSection(int section) {
    openSections.remove(section);
  }
}
