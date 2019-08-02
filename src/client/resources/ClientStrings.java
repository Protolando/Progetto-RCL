package client.resources;

import java.util.ListResourceBundle;

public class ClientStrings extends ListResourceBundle {

  @Override
  protected Object[][] getContents() {
    return new Object[][]{
        {"LoginBtn", "Login"},
        {"RegisterBtn", "Register"},
        {"WindowName", "TURING Client"},
        {"Username", "Username"},
        {"Password", "Password"},
        {"CommitEdits", "Salva"},
        {"ChatSend", "Invia"},
        {"Quit", "Esci"},
        {"Edit", "Modifica Sezione"},
        {"Show", "Mostra sezione"},
        {"ShowWhole", "Mostra tutto"},
        {"Update", "Aggiorna Lista"},
        {"Section", "Sezione"},
        {"Logout", "Disconnetti"},
        {"UsernameUsed", "Username gia` utilizzato"},
        {"ServerError", "Errore di connessione al server"},
        {"RegistrationSuccessful", "Registrazione avvenuta con successo"},
        {"IllegalArgument", "Contenuto dei campi non valido"},
    };
  }
}
