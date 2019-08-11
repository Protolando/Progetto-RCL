package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import share.Request;
import share.RequestType;
import share.UserNotFoundException;
import share.WrongPasswordException;

public class ServerTask implements Runnable {

  private final TURINGServer server;
  private final Request request;
  private final SelectionKey key;

  public ServerTask(TURINGServer server, Request request, SelectionKey key) {
    this.server = server;
    this.request = request;
    this.key = key;
  }

  @Override
  public void run() {
    System.out.println("Processando richiesta: " + request.getRequestType());

    Request reply = new Request(request.getRequestType());
    LoggedUser user = server.getUserFromKey(key);

    switch (request.getRequestType()) {
      case LOGIN:
        if (request.getPayload() == null) {
          /*Se non c'e` il payload...*/
          reply.putInPayload("Message", "Parametri non validi");
        } else {
          String username = request.getPayload().get("Username");
          String password = request.getPayload().get("Password");

          if (password == null || username == null || username.equals("") || password.equals("")) {
            /*... o i parametri sono invalidi*/
            reply.putInPayload("Message", "Parametri non validi");
          } else {
            if (!UsersManager.isRegistered(username)) {
              reply.putInPayload("Message", "Utente non trovato");
            } else if (server.isLoggedIn(username)) {
              reply.putInPayload("Message", "Utente gia` connesso");
            } else {
              try {
                server.login(username, password, key);
                reply.putInPayload("Message", "OK");
              } catch (WrongPasswordException e) {
                reply.putInPayload("Message", "Password errata");
              } catch (UserNotFoundException e) {
                reply.putInPayload("Message", "Utente non trovato");
              }

            }
          }
        }
        break;
      case LOGOUT:
        server.logout(user.getUsername());
        break;
      case CREATE:
        if (request.getPayload() == null) {
          /*Se non c'e` il payload...*/
          reply.putInPayload("Message", "Parametri non validi");
        } else {
          String filename = request.getPayload().get("Filename");
          int nSections;
          try {
            nSections = Integer.parseInt(request.getPayload().get("nSections"));
          } catch (NumberFormatException e) {
            reply.putInPayload("Message", "Parametri non validi");
            break;
          }
          if (filename == null || filename.equals("") || nSections == 0) {
            /*... o i parametri sono invalidi*/
            reply.putInPayload("Message", "Parametri non validi");
          } else {
            try {
              UsersManager
                  .newFile(UsersManager.buildUserPath(user.getUsername()), filename, nSections);
            } catch (IOException e) {
              reply.putInPayload("Message", "Errore nella crazione del file");
            }
          }
        }
        break;
      case INVITE:
        if (user.getUsername() == null) {
          reply.putInPayload("Message", "Utente non connesso");
        } else {
          String filename = request.getPayload().get("filename");
          String username = request.getPayload().get("username");
          try {
            UsersManager.addInvite(user.getUsername(), filename, username);
          } catch (UserNotFoundException | IOException e) {
            reply.putInPayload("Message", "Utente non trovato");
          }
        }
        break;
      case LIST:
        if (user == null) {
          reply.putInPayload("Message", "Utente non connesso");
        } else {
          String[] filesList = new String[0];
          try {
            filesList = UsersManager.getUserFiles(user.getUsername());
          } catch (UserNotFoundException e) {
            reply.putInPayload("Message", "Utente non trovato");
          }
          reply.putInPayload("nfiles", String.valueOf(filesList.length));
          for (int i = 0; i < filesList.length; i++) {
            reply.putInPayload(String.valueOf(i), filesList[i]);
          }
        }
        break;
      case EDIT:
        if (user == null) {
          reply.putInPayload("Message", "Utente non connesso");
        } else {
          String filename = request.getPayload().get("filename");
          int nSection = Integer.parseInt(request.getPayload().get("nSection"));

          String username = user.getUsername();
          String ownername;
          if (!filename.contains("/")) {
            ownername = username;
          } else {
            ownername = filename.substring(0, filename.indexOf('/'));
            filename = filename.substring(filename.indexOf('/') + 1);
          }

          if (!server.canBeEdited(filename, ownername, nSection)) {
            reply.putInPayload("Message", "File gia` in uso da un altro utente");
          } else {
            try {
              ServerFile file = server
                  .addToEditing(filename, ownername, nSection, user.getUsername());
              reply.putInPayload("file", UsersManager.getFile(ownername, filename, nSection));
              reply.putInPayload("MulticastAddress", String.valueOf(file.getMulticastAddress()));
            } catch (IOException e) {
              reply.putInPayload("Message", "File non trovato");
            }
          }
        }
        break;
      case SHOW_SECTION:
        if (user == null) {
          reply.putInPayload("Message", "Utente non connesso");
        } else {
          String filename = request.getPayload().get("filename");
          String username;
          if (!filename.contains("/")) {
            username = user.getUsername();
          } else {
            username = filename.substring(0, filename.indexOf('/'));
            filename = filename.substring(filename.indexOf('/') + 1);
          }
          int nSection = Integer.parseInt(request.getPayload().get("nSection"));
          try {
            reply.putInPayload("file", UsersManager.getFile(username, filename, nSection));
          } catch (IOException e) {
            reply.putInPayload("Message", "File non trovato");
          }
        }
        break;
      case END_EDIT:
        String document = request.getPayload().get("document");
        try {
          server.writeFile(user.getUsername(), document);
        } catch (IOException ignored) {/*Client didn't ask for a reply*/}
        break;
      case SHOW_DOCUMENT:
        if (user == null) {
          reply.putInPayload("Message", "Utente non connesso");
        } else {
          String filename = request.getPayload().get("filename");
          String username = user.getUsername();
          String owner;
          if (!filename.contains("/")) {
            owner = username;
          } else {
            owner = filename.substring(0, filename.indexOf('/'));
            filename = filename.substring(filename.indexOf('/') + 1);
          }
          int totSections = UsersManager.getNSections(UsersManager.buildFilePath(owner, filename));
          StringBuilder res = new StringBuilder();
          try {
            for (int i = 0; i < totSections; i++) {
              res.append("Sezione ").append(i).append("\n\n")
                  .append(UsersManager.getFile(username, filename, i)).append("\n\n");
            }
          } catch (IOException e) {
            reply.putInPayload("Message", "File non trovato");
            break;
          }
          reply.putInPayload("file", res.toString());
        }
        break;
      case GET_MESSAGES:
        break;
      case SEND_MESSAGE:
        break;
      default:
        reply.setRequestType(RequestType.SERVER_RESPONSE);
        reply.putInPayload("Message", "Invalid Request: " + request.getRequestType());
    }

    /*Se il tipo e` logout non devo rispondere*/
    if (request.getRequestType() != RequestType.LOGOUT
        && request.getRequestType() != RequestType.CREATE
        && request.getRequestType() != RequestType.END_EDIT) {
      /*Inizializzo il mapper per JSON*/
      ObjectMapper mapper = new ObjectMapper();
      try {
        /*Converto la risposta in JSON string e la attacco alla chiave*/
        String strReply = mapper.writeValueAsString(reply);
        key.attach(strReply);
      } catch (
          JsonProcessingException e) {
        e.printStackTrace();
      }

      server.addInterestToKey(key, SelectionKey.OP_WRITE);
    }
  }
}
