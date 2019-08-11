package server;

public class FileSection {

  private String openBy;
  private int nSection;
  private ServerFile file;

  public FileSection(int nSection, String openBy, ServerFile file) {
    this.openBy = openBy;
    this.nSection = nSection;
    this.file = file;
  }

  public String getOpenBy() {
    return openBy;
  }

  public int getNSection() {
    return nSection;
  }

  public String getFileName() {
    return file.getFilename();
  }

  public ServerFile getFile(){
    return file;
  }
}
