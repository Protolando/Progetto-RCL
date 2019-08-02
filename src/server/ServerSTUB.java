package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import share.TURINGRegister;

public class ServerSTUB {

  private static final int ServerPort = 4562;

  public static void main(String[] args) {
    System.out.println("Server partito");
    HashMap<String, String> users = new HashMap<>();

    /*Inizializzazione del registry per l'RMI (Registrazione)*/

    try {
      TURINGRegister stub = new RegistrationServer(users);
      LocateRegistry.createRegistry(3141);
      Registry r = LocateRegistry.getRegistry(3141);
      r.rebind(TURINGRegister.SERVICE_NAME, stub);
      System.out.println("Registration Server pronto.");
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      ServerSocket serverSocket = new ServerSocket(ServerPort);

      //noinspection InfiniteLoopStatement
      while (true) {
        Socket s = serverSocket.accept();
        System.out.println("Client Connesso");
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        try {
          System.out.println(in.readObject().toString());
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
