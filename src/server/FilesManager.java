package server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

class FilesManager {
  /*Struttura dati che mantiene i file aperti dagli utenti */

  /*HashMap nome file -> file (hash con collisioni perchè ci possono essere più file con lo
    stesso nome ma owner diversi, che sono quindi file diversi)*/
  private final ConcurrentHashMap<String, ConcurrentLinkedQueue<ServerFile>> files;

  FilesManager() {
    this.files = new ConcurrentHashMap<>();
  }

  void addFile(ServerFile file) {
    /*Aggiunge il file alla struttura*/

    /*Se non c'è un file con il nome uguale a quello passato come parametro nell'hashmap,
     * creo una nuova linked list */
    files.computeIfAbsent(file.getFilename(), k -> new ConcurrentLinkedQueue<>());

    /*Per tutti i file nella lista dei file con il nome uguale a quello del file passato come
     * parametro */
    for (ServerFile f : files.get(file.getFilename())) {
      /*Se l'owner è uguale esco (il file è già presente)*/
      if (file.getOwner().equals(f.getOwner())) {
        return;
      }
    }

    /*Altrimenti lo inserisco nella lista*/
    files.get(file.getFilename()).add(file);
  }

  FileSection getOpenEdits(String username) {
    /*Restituisce la sezione che l'utente passato come parametro ha aperto in edit*/

    /*Cerca tra tutti i file aperti sequenzialmente*/
    for (ConcurrentLinkedQueue<ServerFile> l : files.values()) {
      for (ServerFile f : l) {
        FileSection[] s = f.getOpenSections();
        for (FileSection section : s) {
          if (section.getOpenBy().equals(username)) {
            return section;
          }
        }
      }
    }
    return null;
  }

  void remove(String filename, String ownername) {
    /*Toglie un file dalla lista*/

    for (ServerFile f : files.get(filename)) {
      if (f.getOwner().equals(ownername)) {
        files.get(filename).remove(f);
        return;
      }
    }
  }

  ServerFile getFile(String filename, String ownername) {
    /*Restituisce il file passato come parametro*/

    if (files.get(filename) == null) {
      return null;
    }

    /*Dalla lista dei file con il filename passato come parametro...*/
    for (ServerFile f : files.get(filename)) {
      /*... cerco quello con l'ownername uguale a quello passato come parametro*/
      if (f.getOwner().equals(ownername)) {
        return f;
      }
    }
    return null;
  }
}
