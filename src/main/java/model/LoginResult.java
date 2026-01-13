package model;

/**
 * Risultato dell'autenticazione applicativa eseguita tramite sp_login.
 * - username: username autenticato
 * - role: ruolo effettivo (UTENTE / CONDUCENTE / GESTORE)
 * - cfConducente: valorizzato solo se role == CONDUCENTE
 */
public class LoginResult {

    private final Role role;

    public LoginResult(Role role) {
        this.role = role;
    }

    public Role getRuolo() { return role; }

    public boolean isConducente() {
        return role == Role.CONDUCENTE;
    }
}