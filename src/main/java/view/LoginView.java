package view;

import model.LoginCredentials;

import java.io.IOException;
import java.util.List;

public class LoginView extends BaseCliView {

    /**
     * Legge credenziali da CLI.
     * - /back (su username o password) => annulla e torna al menu precedente (return null)
     * - username vuoto => errore e riprova (NON annulla)
     * - password vuota  => errore e riprova (NON annulla)
     */
    public LoginCredentials readCredentials() {

        printBox("LOGIN", List.of(
                "Digita /back e premi INVIO per tornare al menu precedente."
        ), 60);

        while (true) {
            // ===== Username =====
            String username;
            try {
                System.out.print("Username: ");
                username = reader.readLine();
            } catch (IOException e) {
                showError("Impossibile leggere l'input (username).");
                return null;
            }

            if (username == null) {
                return null; // EOF / input chiuso
            }

            username = username.trim();

            if (username.equalsIgnoreCase("/back")) {
                return null; // annullato esplicitamente
            }

            if (username.isBlank()) {
                showError("Username obbligatorio. Per annullare: /back");
                // qui NON annullo: riparto dal while(true)
                continue;
            }

            // ===== Password =====
            while (true) {
                String password;
                try {
                    System.out.print("Password: ");
                    password = reader.readLine();
                } catch (IOException e) {
                    showError("Impossibile leggere l'input (password).");
                    return null;
                }

                if (password == null) {
                    return null;
                }

                password = password.trim();

                if (password.equalsIgnoreCase("/back")) {
                    return null;
                }

                if (password.isBlank()) {
                    showError("Password obbligatoria. Per annullare: /back");
                    // riprovo solo la password (non rimetto username)
                    continue;
                }

                return new LoginCredentials(username, password);
            }
        }
    }

    /**
     * Messaggio informativo: SOLO stampa (niente pausa qui).
     */
    public void showMessage(String msg) {
        printBox("Info", List.of(msg), 60);
    }

    /**
     * Messaggio di errore: SOLO stampa (niente pausa qui).
     */
    public void showError(String msg) {
        printBox("Errore", List.of(msg), 60);
    }
}