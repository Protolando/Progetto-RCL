package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import share.UsernameAlreadyUsedException;

public class UsersManager {

  public static final String FILES_PATH = "TURINGFiles/";

  public static void register(String username, String password)
      throws UsernameAlreadyUsedException, IOException {
    if (isRegistered(username)) {
      throw new UsernameAlreadyUsedException();
    }

    User user = new User(username, password);
    ObjectMapper mapper = new ObjectMapper();
    FilesManager.writeToFile(buildUserFilePath(username), mapper.writeValueAsString(user));
  }

  public static boolean isRegistered(String username) {
    try {
      FilesManager.readFromFile(buildUserFilePath(username));
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  public static String[] findUserFiles(String username) {
    return FilesManager.getFilesInDir(buildUserPath(username));
  }

  static String buildUserPath(String username) {
    return FILES_PATH + username + "/";
  }

  public static String buildUserFilePath(String username) {
    return buildUserPath(username) + username + ".data";
  }
}
