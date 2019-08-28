package server;

public class FileSection {
  /*Classe che rappresenta la sezione di un file di TURING*/

  /*Nome dell'utente che ha aperto la sezione in modifica*/
  private final String openBy;
  /*Numero di sezione*/
  private final int nSection;
  /*File di cui la sezione fa parte*/
  private final ServerFile file;

  FileSection(int nSection, String openBy, ServerFile file) {
    this.openBy = openBy;
    this.nSection = nSection;
    this.file = file;
  }

  String getOpenBy() {
    return openBy;
  }

  int getNSection() {
    return nSection;
  }

  String getFileName() {
    return file.getFilename();
  }

  public ServerFile getFile(){
    return file;
  }
}
