package model;

/**
 * Risultato dell'autenticazione applicativa eseguita tramite sp_login.
 * - username: username autenticato
 * - role: ruolo effettivo (UTENTE / CONDUCENTE / GESTORE)
 * - cfConducente: valorizzato solo se role == CONDUCENTE
 */
public class LoginResult {

    private final String username;
    private final Role role;
    private final String cfConducente;

    public LoginResult(String username, Role role, String cfConducente) {
        this.username = username;
        this.role = role;
        this.cfConducente = cfConducente;
    }

    public String getUsername() { return username; }
    public Role getRuolo() { return role; }
    public String getCfConducente() { return cfConducente; }

    public boolean isConducente() {
        return role == Role.CONDUCENTE;
    }
}