package server;

public class ServerFile {

  private String owner;
  private String fileName;
  private String openBy;
  private int nSection;

  public ServerFile(String owner, String fileName, int nSection, String openBy) {
    this.fileName = fileName;
    this.openBy = openBy;
    this.nSection = nSection;
    this.owner = owner;
  }

  public String getOwner() {
    return owner;
  }

  public String getFileName() {
    return fileName;
  }

  public String getOpenBy() {
    return openBy;
  }

  public int getNSection() {
    return nSection;
  }

  public boolean equals(ServerFile file) {
    return (getOwner().equals(file.getOwner()) && getFileName().equals(file.getFileName())
        && getNSection() == file.getNSection());
  }
}
