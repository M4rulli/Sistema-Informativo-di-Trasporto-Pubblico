package model;

/**
 * Credenziali applicative digitate dall'utente.
 */
public class LoginCredentials {

    private final String username;
    private final String password;

    public LoginCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public boolean isValid() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }
}