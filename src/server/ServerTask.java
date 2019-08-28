package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.file.FileAlreadyExistsException;
import share.Request;
import share.RequestType;
import share.UserNotFoundException;
import share.WrongPasswordException;

public class ServerTask implements Runnable {
  /*Classe che rappresenta un task eseguito dal server. Viene sottomesso al threadpool dal main thread*/

  /*Server*/
  private final TURINGServer server;
  /*Richiesta che il task deve eseguire*/
  private final Request request;
  /*Selection key da cui è arrivata la richiesta*/
  private final SelectionKey key;

  ServerTask(TURINGServer server, Request request, SelectionKey key) {
    this.server = server;
    this.request = request;
    this.key = key;
  }

  @Override
  public void run() {
    System.out.println("Processando richiesta: " + request.getRequestType());

    /*Inizializzo la risposta*/
    Request reply = new Request(request.getRequestType());
    /*Scopro chi è l'utente relativo alla chiave*/
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
              /*Utente non registrato*/
              reply.putInPayload("Message", "Utente non trovato");
            } else if (server.isLoggedIn(username)) {
              /*Utente già connesso*/
              reply.putInPayload("Message", "Utente gia` connesso");
            } else {
              try {
                /*Provo a fare il login*/
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
        /*Effettuo il logout*/
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
            /*Leggo il numero di sezioni che il file deve avere*/
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
              /*Provo a creare il file*/
              UsersManager.newFile(user.getUsername(), filename, nSections);
            } catch (FileAlreadyExistsException e) {
              reply.putInPayload("Message", "File già esistente");
            } catch (IOException e) {
              reply.putInPayload("Message", "Errore nella creazione del file");
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
            /*Provo ad invitare l'utente*/
            UsersManager.addInvite(user.getUsername(), filename, username);
            /*E se ci riesco provo a notificarlo forse idk TODO*/
          } catch (IOException | UserNotFoundException e) {
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
            /*Leggo la lista dei file dell'utente*/
            filesList = UsersManager.getUserFiles(user.getUsername());
          } catch (UserNotFoundException e) {
            reply.putInPayload("Message", "Utente non trovato");
          }

          /*Inserisco il numero dei file nel payload*/
          reply.putInPayload("nfiles", String.valueOf(filesList.length));
          for (int i = 0; i < filesList.length; i++) {
            /*Inserisco i nomi dei file nel payload uno alla volta*/
            reply.putInPayload(String.valueOf(i), filesList[i]);
          }
        }
        break;
      case EDIT:
        if (user == null) {
          reply.putInPayload("Message", "Utente non connesso");
        } else {
          /*Leggo il nome del file*/
          String filename = request.getPayload().get("filename");
          if (request.getPayload().get("nSection").equals("")) {
            reply.putInPayload("Message", "Numero di sessione non valido");
            break;
          }
          /*Leggo il numero di sezione*/
          int nSection = Integer.parseInt(request.getPayload().get("nSection"));

          String username = user.getUsername();
          String ownername;
          if (!filename.contains("/")) {
            /*Se il nome del file non contiene / allora l'owner è l'utente che ha chiesto la modifica*/
            ownername = username;
          } else {
            /*altrimenti l'owner è scritto prima dello / */
            ownername = filename.substring(0, filename.indexOf('/'));
            filename = filename.substring(filename.indexOf('/') + 1);
          }

          if (!server.canBeEdited(filename, ownername, nSection)) {
            /*Controllo che la sezione non sia aperta in modifica da nessuno*/
            reply.putInPayload("Message", "File gia` in uso da un altro utente");
          } else {
            try {
              /*Aggiungo il file alla lista dei file aperti in edit*/
              ServerFile file = server
                  .addToEditing(filename, ownername, nSection, user.getUsername());
              /*Inserisco nel payload l'indirizzo della chat e il contenuto attuale del file*/
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
          if (request.getPayload().get("nSection").equals("")) {
            reply.putInPayload("Message", "Numero di sessione non valido");
            break;
          }
          /*Leggo nome del file*/
          String filename = request.getPayload().get("filename");
          String username;
          /*Come per l'edit, cerco nome del file e dell'owner*/
          if (!filename.contains("/")) {
            username = user.getUsername();
          } else {
            username = filename.substring(0, filename.indexOf('/'));
            filename = filename.substring(filename.indexOf('/') + 1);
          }

          /*Leggo il numero di sezione*/
          int nSection = Integer.parseInt(request.getPayload().get("nSection"));

          try {
            /*Inserisco il file nel payload*/
            reply.putInPayload("file", UsersManager.getFile(username, filename, nSection));
          } catch (IOException e) {
            reply.putInPayload("Message", "File non trovato");
          }
        }
        break;
      case END_EDIT:
        /*Leggo il documento dal payload*/
        String document = request.getPayload().get("document");
        try {
          /*Provo a scriverlo su file*/
          server.writeFile(user.getUsername(), document);
        } catch (IOException e) {
          reply.putInPayload("Message", "Errore nel salvataggio del file");
        }
        /*No break, continuo*/
      case CANCEL_EDIT:
        /*Tolgo il file dalla lista dei file aperti in modifica*/
        server.removeFromEditing(user.getUsername());
        break;
      case SHOW_DOCUMENT:
        if (user == null) {
          reply.putInPayload("Message", "Utente non connesso");
        } else {
          /*Leggo il nome del file dal payload*/
          String filename = request.getPayload().get("filename");
          String username = user.getUsername();
          String owner;
          /*prendo nome utente e nome owner come per edit*/
          if (!filename.contains("/")) {
            owner = username;
          } else {
            owner = filename.substring(0, filename.indexOf('/'));
            filename = filename.substring(filename.indexOf('/') + 1);
          }

          /*Scopro il numero di sezioni del file*/
          /*Leggo ogni sezione e la inserisco in una stringa*/
          StringBuilder res = new StringBuilder();
          try {
            int totSections = UsersManager.getNSections(owner, filename);
            for (int i = 0; i < totSections; i++) {
              res.append("Sezione ").append(i).append("\n\n")
                  .append(UsersManager.getFile(username, filename, i)).append("\n\n");
            }
          } catch (IOException e) {
            reply.putInPayload("Message", "File non trovato");
            break;
          }
          /*Metto il file nel payload*/
          reply.putInPayload("file", res.toString());
        }
        break;
      default:
        /*Richiesta invalida*/
        reply.setRequestType(RequestType.SERVER_RESPONSE);
        reply.putInPayload("Message", "Richiesta invalida: " + request.getRequestType());
    }

    /*Mando la risposta*/
    if (request.getRequestType()
        != RequestType.LOGOUT) { //Se la richiesta era logout, non devo rispondere
      /*Inizializzo il mapper per JSON*/
      ObjectMapper mapper = new ObjectMapper();
      try {
        /*Converto la risposta in JSON string e la attacco alla chiave*/
        String strReply = mapper.writeValueAsString(reply);
        synchronized (key) { /*TODO DEVO?*/
          key.attach(strReply);
          /*Aggiungo la chiave a quelle scrivibili*/
          server.addInterestToKey(key, SelectionKey.OP_WRITE);
        }
      } catch (
          JsonProcessingException e) {
        e.printStackTrace();
      }

    }
  }
}
