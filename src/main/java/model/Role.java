package model;

/**
 * Ruoli previsti dal sistema.
 * Nota: LOGIN è l'account DB usato solo per eseguire sp_login.
 */
public enum Role {
    LOGIN,
    UTENTE,
    CONDUCENTE,
    GESTORE
}