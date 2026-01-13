package model;

/**
 * Utente applicativo.
 *
 * Nel DB esiste la tabella Utenti (username, password_hash, ruolo) e,
 * per i soli CONDUCENTE, un collegamento 1:1 verso Conducente(cf).
 */
public class User {

    private final String username;
    private final Role role;
    private final String cfConducente; // null se non conducente

    public User(String username, Role role, String cfConducente) {
        this.username = username;
        this.role = role;
        this.cfConducente = cfConducente;
    }

    public String getUsername() { return username; }
    public Role getRuolo() { return role; }
    public String getCfConducente() { return cfConducente; }
}