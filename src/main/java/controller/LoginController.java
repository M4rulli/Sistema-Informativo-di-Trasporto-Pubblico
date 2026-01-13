package controller;

import dao.LoginDAO;
import exceptions.DatabaseException;
import model.LoginCredentials;
import model.LoginResult;
import model.Role;
import view.LoginView;

import java.sql.SQLException;

/**
 * Controller del login.
 * Responsabilità: orchestrare la richiesta credenziali (View) e la chiamata alla stored procedure (DAO).
 */
public class LoginController {

    private final LoginView view;

    public LoginController(LoginView view) {
        this.view = view;
    }

    /**
     * Esegue un tentativo di login.
     * @return LoginResult se login OK; null se annullato (/back) o fallito.
     */
    public LoginResult loginOnce() {
        LoginCredentials credentials = view.readCredentials();

        // ✅ annullato con /back (o EOF): torno al menu principale senza crash e senza "premi invio"
        if (credentials == null) {
            System.out.println("Login annullato, torno al menu principale.");
            return null;
        }

        try {
            LoginDAO loginDAO = new LoginDAO(credentials);
            LoginResult res = loginDAO.execute();

            if (res == null) {
                view.showError("Credenziali non valide.");
                return null;
            }

            Role role = res.getRuolo();
            view.showMessage("Login effettuato con successo. Ruolo: " + role);
            return res;

        } catch (DatabaseException ex) {
            Throwable c = ex.getCause();
            if (c instanceof SQLException se) {
                view.showError(
                        "Errore DB: SQLState=" + se.getSQLState() +
                                " errno=" + se.getErrorCode() +
                                " msg=" + se.getMessage()
                );
            } else {
                view.showError("Errore durante il login: " + ex.getMessage());
            }
            return null;
        }
    }
}