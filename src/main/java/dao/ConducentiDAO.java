package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;

public class ConducentiDAO {

    public void inserisciConducente(String cf, String nome, String cognome,
                                    LocalDate dataNascita, String luogoNascita,
                                    String numeroPatente, LocalDate scadenzaPatente) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op06a_inserisci_conducente(?, ?, ?, ?, ?, ?, ?)}")) {

            cs.setString(1, cf);
            cs.setString(2, nome);
            cs.setString(3, cognome);
            cs.setDate(4, Date.valueOf(dataNascita));
            cs.setString(5, luogoNascita);
            cs.setString(6, numeroPatente);
            cs.setDate(7, Date.valueOf(scadenzaPatente));

            cs.execute();

        } catch (SQLException e) {
            throw new DatabaseException(String.format("Errore %d: \"%s\"", e.getErrorCode(), safeSqlMessage(e)), e);
        }
    }

    public void aggiornaConducente(String cf, String nome, String cognome,
                                   LocalDate dataNascita, String luogoNascita,
                                   String numeroPatente, LocalDate scadenzaPatente) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op06b_aggiorna_conducente(?, ?, ?, ?, ?, ?, ?)}")) {

            cs.setString(1, cf);

            if (nome == null) cs.setNull(2, Types.VARCHAR); else cs.setString(2, nome);
            if (cognome == null) cs.setNull(3, Types.VARCHAR); else cs.setString(3, cognome);
            if (dataNascita == null) cs.setNull(4, Types.DATE); else cs.setDate(4, Date.valueOf(dataNascita));
            if (luogoNascita == null) cs.setNull(5, Types.VARCHAR); else cs.setString(5, luogoNascita);
            if (numeroPatente == null) cs.setNull(6, Types.VARCHAR); else cs.setString(6, numeroPatente);
            if (scadenzaPatente == null) cs.setNull(7, Types.DATE); else cs.setDate(7, Date.valueOf(scadenzaPatente));

            cs.execute();

        } catch (SQLException e) {
            throw new DatabaseException(String.format("Errore %d: \"%s\"", e.getErrorCode(), safeSqlMessage(e)), e);
        }
    }

    private String safeSqlMessage(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return "(nessun messaggio)";
        return msg.replace("\n", " ").replace("\r", " ");
    }
}