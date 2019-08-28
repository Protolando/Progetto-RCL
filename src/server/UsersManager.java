package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import share.UserNotFoundException;
import share.UsernameAlreadyUsedException;

public class UsersManager {
  /*Classe statica che gestisce i file degli utenti*/

  /*Percorso dei file su disco*/
  static final String FILES_PATH = "TURINGFiles";
  private static final int BUFFER_SIZE = 1024;

  /************************************ Registrazione ************************************/

  public static void register(String username, String password)
      throws UsernameAlreadyUsedException, IOException {
    /*Crea i file dell'utente su disco*/

    if (isRegistered(username)) {
      throw new UsernameAlreadyUsedException();
    }

    /*Creo un nuovo user*/
    User user = new User(username, password);
    /*Lo converto in JSON e lo scrivo su file come .data*/
    ObjectMapper mapper = new ObjectMapper();
    writeToFile(buildUserFilePath(username), mapper.writeValueAsString(user));
  }

  static boolean isRegistered(String username) {
    /*Restituisce true se l'utente non è iscritto*/

    try {
      /*Se viene restituita un'eccezione, i file non esistono su disco*/
      readFromFile(buildUserFilePath(username));
    } catch (IOException e) {
      return false;
    }

    /*Se non è stata restituita un'eccezione i file esistevano, quindi l'utente è registrato*/
    return true;
  }

  /******************************* Costruzione del percorso ai file *******************************/

  private static String buildUserPath(String username) {
    /*Crea il percorso della cartella dell'utente*/
    return FILES_PATH + "/" + username;
  }

  private static String buildUserFilePath(String username) {
    /*Crea il percorso per il file .data dell'utente*/
    return buildUserPath(username) + "/" + username + ".data";
  }

  private static String buildFilePath(String owner, String filename) {
    /*Restituisce il percorso di un file*/
    return buildUserPath(owner) + "/" + filename + ".TURINGFile/";
  }

  private static String buildSectionPath(String owner, String filename, int nSection) {
    /*Restituisce il percorso al file di sezione passato come parametro*/
    return buildFilePath(owner, filename) + nSection + ".section";
  }

  /************************** Modifica/lettura File di Dati dell'Utente **************************/

  static User getUserData(String username) throws UserNotFoundException {
    /*Legge da disco il file .data dell'utente e lo restituisce*/
    ObjectMapper mapper = new ObjectMapper();

    User user;
    try {
      user = mapper.readValue(readFromFile(buildUserFilePath(username)), User.class);
    } catch (IOException e) {
      throw new UserNotFoundException();
    }

    return user;
  }

  static void addInvite(String owner, String filename, String username)
      throws IOException, UserNotFoundException {
    if (!isRegistered(owner)) {
      throw new UserNotFoundException();
    }

    boolean found = false;
    String[] files = getUserFiles(owner);
    for (String s : files) {
      if (s.equals(filename)) {
        found = true;
        break;
      }
    }

    if (!found) {
      throw new FileNotFoundException();
    }

    /*Aggiunge un invito ad un utente*/
    User userfile = getUserData(username);

    userfile.addInvite(owner + "/" + filename);
    writeUserFile(username, userfile);
  }

  private static void writeUserFile(String username, User userfile) throws IOException {
    /*Scrive su disco un file di un utente*/
    String userpath = buildUserFilePath(username);
    Path path = Paths.get(userpath);
    path.toFile().delete();

    ObjectMapper mapper = new ObjectMapper();
    writeToFile(userpath, mapper.writeValueAsString(userfile));
  }

  /************************************ Modifica/lettura file ************************************/

  static String getFile(String username, String filename, int nSection) throws IOException {
    /*Restituisce il file di sezione passato come parametro*/
    return readFromFile(buildSectionPath(username, filename, nSection));
  }

  static String[] getUserFiles(String username) throws UserNotFoundException {
    /*Restituisce la lista dei file a cui l'utente ha accesso*/

    if (!isRegistered(username)) {
      throw new UserNotFoundException();
    }

    /*leggo la lista di file di cui l'utente è owner*/
    String[] userFiles = getFilesInDir(buildUserPath(username));
    int nUserFiles; //Lunghezza della lista
    if (userFiles == null) {
      nUserFiles = 0;
    } else {
      nUserFiles = userFiles.length;
    }

    /*Leggo il file dell'utente*/
    User user;
    user = getUserData(username);

    /*Leggo il numero degli inviti*/
    int nUserInvited;
    if (user.getInvites() == null) {
      nUserInvited = 0;
    } else {
      nUserInvited = user.getInvites().length;
    }

    /*Aggiungo ogni invito alla lista dei file che devo restituire*/
    String[] files = new String[nUserFiles + nUserInvited];
    if (userFiles != null) {
      System.arraycopy(userFiles, 0, files, 0, nUserFiles);
    }
    if (user.getInvites() != null) {
      System.arraycopy(user.getInvites(), 0, files, nUserFiles, nUserInvited);
    }

    return files;
  }

  static void writeToFile(FileSection section, String data) throws IOException {
    /*Scrive su disco il file passato come parametro*/
    writeToFile(buildSectionPath(section.getFile().getOwner(), section.getFile().getFilename(),
        section.getNSection()), data);
  }

  static void newFile(String owner, String filename, int nSections) throws IOException {
    /*Crea un nuovo .TURINGFile*/
    String filepath = UsersManager.buildUserPath(owner);

    String actualFilePath = filepath + "/" + filename + ".TURINGFile";
    Path path = Paths.get(actualFilePath);

    /*Controllo che il file non esista già*/
    if (Files.exists(path.subpath(0, path.getNameCount()))) {
      throw new FileAlreadyExistsException(null);
    }

    /*Creo tutte le cartelle per il percorso*/
    Files.createDirectories(path.subpath(0, path.getNameCount()));

    /*Creo un file vuoto per ogni sezione (1.section, 2.section, ...)*/
    for (int i = 0; i < nSections; i++) {
      Path sectionPath = Paths.get(actualFilePath + "/" + i + ".section");
      if (!Files.exists(sectionPath, LinkOption.NOFOLLOW_LINKS)) {
        Files.createFile(sectionPath);
      }
    }
  }

  static int getNSections(String owner, String filename) throws FileNotFoundException {
    /*Restituisce il numero di sezioni che ha un file*/
    return getNSections(buildFilePath(owner, filename));
  }

  /************************************ Interazione filesystem ************************************/

  static String readFromFile(String path) throws IOException {
    /*Leggo il file passato come parametro*/

    /*Creo il canale*/
    FileChannel in = FileChannel.open(Paths.get(path), StandardOpenOption.READ);

    /*Inizializzo il buffer*/
    ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    Charset charset = StandardCharsets.UTF_8;

    /*Leggo tutto il file e lo salvo in una stringa*/
    StringBuilder builder = new StringBuilder();
    /*finche` posso ancora leggere*/
    /*Leggo*/
    buffer.clear();
    while (in.read(buffer) != -1) {
      /*Svuoto il buffer*/
      buffer.flip();
      builder.append(charset.decode(buffer));
      buffer.clear();
    }
    return builder.toString();
  }

  static void writeToFile(String filepath, String data) throws IOException {
    /*Scrive su disco il file passato come parametro (overload)*/

    /*Prendo il percorso del file*/
    Path path = Paths.get(filepath);
    /*Creo tutte le cartelle per il percorso (-1 perche` non voglio creare una cartella con il nome del file)*/
    Files.createDirectories(path.subpath(0, path.getNameCount() - 1));
    /*Se non esiste creo anche il file*/
    if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
      Files.createFile(path);
    }

    /*Apro il canale*/
    FileChannel out = FileChannel.open(path, StandardOpenOption.WRITE);

    /*Inizializzo buffer*/
    ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    Charset charset = StandardCharsets.UTF_8;

    ByteBuffer src = charset.encode(data);
    /*Riempio il buffer fino a capacita`, lo scrivo e ripeto finche` non ho mandato tutti i dati*/
    int i = 0;
    while (i < src.limit()) {
      /*Riempio il buffer*/
      buffer.clear();
      buffer.put(src.array(), i, Math.min(src.limit() - i, BUFFER_SIZE));
      /*E lo scrivo*/
      buffer.flip();
      i += out.write(buffer);
    }

    /*Chiudo il canale*/
    out.close();
  }

  private static int getNSections(String path) throws FileNotFoundException {
    /*Restituisce il numero di sezioni che ha un file*/
    if (!Files.exists(Paths.get(path))) {
      throw new FileNotFoundException();
    }

    try (Stream<Path> stream = Files.walk(Paths.get(path))) {
      /*Cerco tutti i file che terminano con .section e ne restituisco il numero*/
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.section");
      return stream
          .filter(file -> (matcher.matches(file.getFileName())))
          .map(Path::getFileName)
          .map(Path::toString)
          .distinct()
          .toArray(String[]::new)
          .length;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private static String[] getFilesInDir(String dirpath) {
    /*Legge la lista di file in una cartella*/

    try (Stream<Path> stream = Files.walk(Paths.get(dirpath))) {
      /*Cerco tutti i file che terminano con .TURINGFile e ne restituisco un array (senza l'estensione)*/
      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.TURINGFile");
      return stream
          .filter(file -> (Files.isDirectory(file) && matcher.matches(file.getFileName())))
          .map(Path::getFileName)
          .map(Path::toString)
          .map(s -> s.substring(0, s.lastIndexOf('.')))
          .distinct()
          .toArray(String[]::new);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
