package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FilesManagerTest {

  @Test
  void TestFileIO() {
    String path = "./TURINGFiles/testfile.txt";
    String message = "test";
    String result = null;

    try {
      FilesManager.writeToFile(path, message);
      result = FilesManager.readFromFile(path);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    assertEquals(message, result);
  }

  @AfterEach
  void tearDown() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }
}