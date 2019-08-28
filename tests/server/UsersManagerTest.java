package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import share.UserNotFoundException;
import share.UsernameAlreadyUsedException;

class UsersManagerTest {

  @BeforeEach
  void setUp() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }

  @AfterEach
  void tearDown() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }

  @Test
  void TestFileIO() {
    String path = "./TURINGFiles/testfile.txt";
    String message = "test";
    String result = null;

    try {
      UsersManager.writeToFile(path, message);
      result = UsersManager.readFromFile(path);
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
    String[] userFiles = new String[0];
    try {
      userFiles = UsersManager.getUserFiles(username);
    } catch (UserNotFoundException e) {
      fail();
    }
    assertEquals(0, userFiles.length);
    try {
      UsersManager.newFile(username, filename, nsections);
    } catch (IOException e) {
      fail();
    }
    try {
      userFiles = UsersManager.getUserFiles(username);
    } catch (UserNotFoundException e) {
      fail();
    }
    assertEquals(1, userFiles.length);
    assertEquals(filename, userFiles[0]);
    try {
      assertEquals(nsections, UsersManager.getNSections(username, filename));
    } catch (FileNotFoundException e) {
      fail();
    }

    /*Provo a ricreare un file che esiste già*/
    try {
      UsersManager.newFile(username, filename, nsections);
      fail();
    } catch (IOException ignored) {
    }
  }

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
      res = UsersManager.getUserFiles(username);
    } catch (Exception ignored) {
    }
    assertNotNull(res);
    assertEquals(0, res.length);
  }

  @Test
  void register() {
    String username = "test";
    String password = "test";

    assertFalse(UsersManager.isRegistered(username));

    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }

    assertTrue(UsersManager.isRegistered(username));
  }

  @Test
  void getUserData() {
    String username = "test";
    String password = "test";

    try {
      assertNull(UsersManager.getUserData(username));
      fail();
    } catch (UserNotFoundException ignored) {
    }

    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }

    User u = null;
    try {
      u = UsersManager.getUserData(username);
    } catch (UserNotFoundException e) {
      fail();
    }

    assertNotNull(u);
    assertEquals(username, u.getUsername());
    assertEquals(password, u.getPassword());
    assertEquals(0, u.getInvites().length);
  }

  @Test
  void addInvite() {
    String username = "test";
    String password = "test";
    String owner = "prova";
    String filename = "prova";

    /*Registro l'utente*/
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }

    /*Leggo il file dell'utente*/
    User u = null;
    try {
      u = UsersManager.getUserData(username);
    } catch (UserNotFoundException e) {
      fail();
    }

    /*Non deve avere inviti*/
    assertEquals(0, u.getInvites().length);

    /*Provo ad aggiungere un invito da un utente non registrato*/
    try {
      UsersManager.addInvite(owner, filename, username);
      fail();
    } catch (IOException | UserNotFoundException ignored) {
    }

    /*Registro un utente e provo ad aggiungere un invito per un file che non esiste*/
    try {
      UsersManager.register(owner, owner);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }

    try {
      UsersManager.addInvite(owner, filename, username);
      fail();
    } catch (IOException | UserNotFoundException ignored) {
    }

    /*Creo il file e provo ad aggiungere un invito. Questa volta deve funzionare*/

    try {
      UsersManager.newFile(owner, filename, 10);
    } catch (IOException e) {
      fail();
    }

    try {
      UsersManager.addInvite(owner, filename, username);
    } catch (IOException | UserNotFoundException ignored) {
      fail();
    }

    /*Controllo che l'invito sia corretto*/
    try {
      u = UsersManager.getUserData(username);
    } catch (UserNotFoundException e) {
      fail();
    }

    assertEquals(1, u.getInvites().length);
    String file = owner + "/" + filename;
    assertEquals(file, u.getInvites()[0]);
  }

  @Test
  void getUserFiles() {
    String username = "test";
    String password = "test";

    /*Provo a leggere i file di un utente che non esiste*/
    try {
      UsersManager.getUserFiles(username);
      fail();
    } catch (UserNotFoundException ignored) {
    }

    /*Registro l'utente*/
    try {
      UsersManager.register(username, password);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }

    /*Creo un po' di file*/
    String filename = "test";
    for (int i = 0; i < 10; i++) {
      try {
        UsersManager.newFile(username, filename + i, 1);
      } catch (IOException e) {
        fail();
      }
    }

    String[] files = new String[0];
    try {
      files = UsersManager.getUserFiles(username);
    } catch (UserNotFoundException e) {
      fail();
    }

    assertNotEquals(0, files.length);
    Arrays.sort(files);
    for (int i = 0; i < 10; i++) {
      assertEquals(filename + i, files[i]);
    }
  }

  @Test
  void getNSections() {
    String username = "test";
    String password = "test";
    String filename = "test";
    int nSections = 20;

    /*Provo a leggere i file di un utente che non esiste*/
    try {
      UsersManager.getNSections(username, filename);
      fail();
    } catch (FileNotFoundException ignored) {
    }

    /*Registro l'utente*/
    try {
      UsersManager.register(username, password);
      UsersManager.newFile(username, filename, nSections);
    } catch (UsernameAlreadyUsedException | IOException e) {
      fail();
    }

    try {
      assertEquals(nSections, UsersManager.getNSections(username, filename));
    } catch (FileNotFoundException e) {
      fail();
    }
  }
}