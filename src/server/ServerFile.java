package server;

import java.util.concurrent.ConcurrentHashMap;

class ServerFile {
  /*Classe che rappresenta un file di TURING*/

  /*Lista delle sezioni aperte indicizzata per numero di sezione*/
  private final ConcurrentHashMap<Integer, FileSection> openSections;
  /*Nome del proprietario*/
  private final String owner;
  /*Nome del file*/
  private final String filename;
  /*Indirizzo della chat*/
  private final String multicastAddress;

  ServerFile(String filename, String owner, String multicastAddress) {
    this.openSections = new ConcurrentHashMap<>();
    this.owner = owner;
    this.filename = filename;
    this.multicastAddress = multicastAddress;
  }

  String getOwner() {
    return owner;
  }

  String getMulticastAddress() {
    return multicastAddress;
  }

  void addOpenSection(FileSection section) {
    openSections.put(section.getNSection(), section);
  }

  FileSection[] getOpenSections() {
    /*Restituisce la lista delle sezioni aperte*/
    return openSections.values().toArray(new FileSection[0]);
  }

  boolean isInUse(int sectionNumber) {
    /*Restituisce true se il numero di sezione passato come parametro è aperto in modifica da un
      utente*/
    return (openSections.get(sectionNumber) != null);
  }

  String getFilename() {
    return filename;
  }

  int nSectionOpen() {
    /*Restituisce il numero di sezioni del file aperte in modifica dagli utenti*/
    return getOpenSections().length;
  }

  void removeSection(int section) {
    /*Toglie la sezione passata come parametro dalla lista di quelle aperte dagli utenti*/
    openSections.remove(section);
  }
}
