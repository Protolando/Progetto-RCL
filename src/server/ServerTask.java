package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import share.Request;
import share.RequestType;
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
    switch (request.getRequestType()) {
      case LOGIN:
        ;
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
            if (!server.isRegistered(username)) {
              reply.putInPayload("Message", "Utente non trovato");
            } else if (server.isLoggedIn(username)) {
              reply.putInPayload("Message", "Utente gia` connesso");
            } else {
              try {
                server.login(username, password);
                reply.putInPayload("Message", "OK");
              } catch (WrongPasswordException e) {
                reply.putInPayload("Message", "Password errata");
              }

            }
          }
        }
        break;
      case LOGOUT:
        break;
      case CREATE:
        break;
      case INVITE:
        break;
      case LIST:
        break;
      case EDIT:
        break;
      case END_EDIT:
        break;
      case SHOW_SECTION:
        break;
      case SHOW_DOCUMENT:
        break;
      case GET_MESSAGES:
        break;
      case SEND_MESSAGE:
        break;
      default:
        reply.setRequestType(RequestType.SERVER_RESPONSE);
        reply.putInPayload("Message", "Invalid Request: " + request.getRequestType());
    }

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
