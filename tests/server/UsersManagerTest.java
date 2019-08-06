package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import share.UsernameAlreadyUsedException;

class UsersManagerTest {


  @Test
  void findUserFiles() {
    String username = "test";
    String password = "test";
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }

    String[] res = null;
    try {
      res = UsersManager.findUserFiles(username);
    } catch (Exception ignored) {
    }
    assertNotNull(res);
    assertEquals(0, res.length);
  }

  @AfterEach
  void tearDown() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }
}