package server;

import java.io.File;

public class CleanDirectory {

  static void deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    directoryToBeDeleted.delete();
  }
}
