package server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilesManagerTest {

  @BeforeEach
  void setUp() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }


  @Test
  void addFile() {
    String filename = "test";
    String nome = "test";
    String multicastAddress = "0.0.0.0";

    FilesManager filesManager = new FilesManager();
    ServerFile file = new ServerFile(filename, nome, multicastAddress);
    filesManager.addFile(file);

    assertEquals(file, filesManager.getFile(filename, nome));

    /*Controllo che ci possano essere più file con lo stesso nome ma owner diversi*/
    String nome2 = "test2";
    ServerFile file2 = new ServerFile(filename, nome2, multicastAddress);
    filesManager.addFile(file2);

    assertNotEquals(file, file2);
    assertEquals(file, filesManager.getFile(filename, nome));
    assertEquals(file2, filesManager.getFile(filename, nome2));

  }

  @Test
  void getOpenEdits() {
    String filename = "test";
    String nome = "test";
    String multicastAddress = "0.0.0.0";

    FilesManager filesManager = new FilesManager();
    ServerFile file = new ServerFile(filename, nome, multicastAddress);
    filesManager.addFile(file);
    FileSection section = new FileSection(0, nome, file);
    file.addOpenSection(section);

    assertEquals(section, filesManager.getOpenEdits(nome));
    String nome2 = "pippo";
    assertNull(filesManager.getOpenEdits(nome2));
  }

  @Test
  void remove() {
    String filename = "test";
    String nome = "test";
    String multicastAddress = "0.0.0.0";

    FilesManager filesManager = new FilesManager();
    ServerFile file = new ServerFile(filename, nome, multicastAddress);
    filesManager.addFile(file);
    filesManager.remove(filename, nome);

    assertNull(filesManager.getFile(filename, nome));
  }

  @AfterEach
  void tearDown() {
    CleanDirectory.deleteDirectory(new File(UsersManager.FILES_PATH));
  }
}