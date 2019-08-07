package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import share.UserNotFoundException;
import share.UsernameAlreadyUsedException;

public class UsersManager {

  public static final String FILES_PATH = "TURINGFiles";

  public static void register(String username, String password)
      throws UsernameAlreadyUsedException, IOException {
    if (isRegistered(username)) {
      throw new UsernameAlreadyUsedException();
    }

    User user = new User(username, password, null);
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
    return FILES_PATH + "/" + username;
  }

  public static String buildUserFilePath(String username) {
    return buildUserPath(username) + "/" + username + ".data";
  }

  public static User getUserData(String username) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(FilesManager.readFromFile(buildUserFilePath(username)), User.class);
  }

  public static String[] getUserFiles(String username) throws UserNotFoundException {
    String[] userFiles = FilesManager.getFilesInDir(buildUserPath(username));
    int nUserFiles;
    if (userFiles == null) {
      nUserFiles = 0;
    } else {
      nUserFiles = userFiles.length;
    }

    User user;
    try {
      user = getUserData(username);
    } catch (IOException e) {
      throw new UserNotFoundException();
    }
    int nUserInvited;
    if (user.getInvites() == null) {
      nUserInvited = 0;
    } else {
      nUserInvited = user.getInvites().length;
    }

    String[] files = new String[nUserFiles + nUserInvited];
    if (userFiles != null) {
      System.arraycopy(userFiles, 0, files, 0, nUserFiles);
    }
    if (user.getInvites() != null) {
      System.arraycopy(user.getInvites(), 0, files, nUserFiles, nUserInvited);
    }

    return files;
  }
}
