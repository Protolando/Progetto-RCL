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
        {"NewFile", "Nuovo File"},
        {"ShowWhole", "Mostra tutto"},
        {"Update", "Aggiorna Lista"},
        {"Invite", "Invita utente"},
        {"Section", "Sezione"},
        {"Logout", "Disconnetti"},
        {"UsernameUsed", "Username gia` utilizzato"},
        {"ServerError", "Errore di connessione al server"},
        {"RegistrationSuccessful", "Registrazione avvenuta con successo"},
        {"IllegalArgument", "Contenuto dei campi non valido"},
        {"ConnectionFailed", "Connessione Fallita"},
        {"tryAgain", "Riprovare"},
        {"section", "Sezione"},
        {"insertSectionNum", "Inserire numero di sezione"},
        {"selectADocument", "Selezionare un documento"},
        {"CreateFile", "Crea File"},
        {"FileName", "Nome File: "},
        {"NumberOfSections", "Numero di Sezioni: "},
        {"WrongParameters", "Parametri errati"},
        {"UnexpectedError", "Errore inatteso"},
        {"insertUser", "Inserire l'utente da invitare"},
        {"youWereDisconnected", "Sei stato disconnesso dal server"},
        {"failSendingMessage", "Invio messaggio di chat fallito"},
    };
  }
}
