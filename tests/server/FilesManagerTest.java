package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import share.UsernameAlreadyUsedException;

class FilesManagerTest {

  @BeforeEach
  void setUp() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }

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

  @Test
  void newFile() {
    String username = "test";
    String password = "test";
    String filename = "myNewFile";
    int nsections = 6;
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }
    String[] userFiles = UsersManager.findUserFiles(username);
    assertEquals(0, userFiles.length);
    try {
      FilesManager.newFile(UsersManager.buildUserPath(username), filename, nsections);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
    userFiles = UsersManager.findUserFiles(username);
    assertEquals(1, userFiles.length);
    assertEquals(filename, userFiles[0]);
    assertEquals(nsections,
        FilesManager.getNSections(UsersManager.buildUserPath(username) + "/" + filename));
  }

  @AfterEach
  void tearDown() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }
}