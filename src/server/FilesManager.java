package server;

import java.util.HashMap;
import java.util.LinkedList;

class FilesManager {

  private HashMap<String, LinkedList<ServerFile>> files;

  public FilesManager() {
    this.files = new HashMap<>();
  }

  public void addFile(ServerFile file) {
    files.computeIfAbsent(file.getFilename(), k -> new LinkedList<>());
    for (ServerFile f : files.get(file.getFilename())) {
      if (file.getOwner().equals(f.getOwner())) {
        return;
      }
    }

    files.get(file.getFilename()).add(file);
  }

  public FileSection getOpenEdits(String username) {
    for (LinkedList<ServerFile> l : files.values()) {
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

  public void remove(String filename, String ownername) {
    for (ServerFile f : files.get(filename)) {
      if (f.getOwner().equals(ownername)) {
        files.get(filename).remove(f);
        return;
      }
    }
  }

  public ServerFile getFile(String filename, String ownername) {
    if (files.get(filename) == null) {
      return null;
    }

    for (ServerFile f : files.get(filename)) {
      if (f.getOwner().equals(ownername)) {
        return f;
      }
    }
    return null;
  }
}
