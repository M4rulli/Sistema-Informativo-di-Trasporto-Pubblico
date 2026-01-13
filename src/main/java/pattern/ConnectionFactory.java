package pattern;

import model.Role;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ConnectionFactory.
 * - Carica credenziali da db.properties
 * - Mantiene un ruolo corrente
 * - Fornisce connessioni JDBC con l'utente DB corretto
 */
public final class ConnectionFactory {

    private static final Properties props = new Properties();

    static {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream input = cl.getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new IllegalStateException("Impossibile trovare db.properties nel classpath.");
            }
            props.load(input);

        } catch (IOException e) {
            throw new IllegalStateException("Errore durante il caricamento di db.properties.", e);
        }
    }

    private ConnectionFactory() {}

    /**
     * Connessione usata esclusivamente per invocare la sp_login.
     */
    public static Connection getConnectionForLogin() throws SQLException {
        return getConnectionForRole(Role.LOGIN);
    }

    /**
     * Connessione con l'utente DB associato al ruolo.
     */
    public static Connection getConnectionForRole(Role role) throws SQLException {
        if (role == null) throw new IllegalArgumentException("Ruolo nullo.");

        String dbUrl = props.getProperty("db.url");
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new SQLException("db.url non trovato in db.properties.");
        }

        String userKey;
        String pwdKey;

        switch (role) {
            case LOGIN -> {
                userKey = "db.user.login";
                pwdKey = "db.password.login";
            }
            case UTENTE -> {
                userKey = "db.user.utente";
                pwdKey = "db.password.utente";
            }
            case CONDUCENTE -> {
                userKey = "db.user.conducente";
                pwdKey = "db.password.conducente";
            }
            case GESTORE -> {
                userKey = "db.user.gestore";
                pwdKey = "db.password.gestore";
            }
            default -> throw new SQLException("Ruolo non supportato: " + role);
        }

        String user = props.getProperty(userKey);
        String password = props.getProperty(pwdKey);

        if (user == null || user.isBlank()) {
            throw new SQLException("Credenziale mancante: " + userKey);
        }
        if (password == null) password = "";

        return DriverManager.getConnection(dbUrl, user, password);
    }
}