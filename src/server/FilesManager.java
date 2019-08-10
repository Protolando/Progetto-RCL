package server;

import java.util.LinkedList;

class FilesManager {

  private LinkedList<ServerFile> files;

  public FilesManager() {
    this.files = new LinkedList<>();
  }

  public void addFile(ServerFile file) {
    files.add(file);
  }

  public ServerFile getOpenEdits(String username) {
    for (ServerFile f : files) {
      if (f.getOpenBy().equals(username)) {
        return f;
      }
    }
    return null;
  }

  public boolean isBeingEdited(String filename) {
    for (ServerFile f : files) {
      if (f.equals(filename)) {
        return true;
      }
    }
    return false;
  }

  public void remove(String owner, String filename, int section) {
    files.remove(new ServerFile(owner, filename, section, null));
  }
}
