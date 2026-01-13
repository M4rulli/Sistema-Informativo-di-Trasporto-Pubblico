package dao;

import exceptions.DatabaseException;
import model.Orario;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

/**
 * DAO OP02 – Prossima partenza al capolinea.
 * Chiama sp_op02_prossima_partenza(numero_tratta, direzione)
 * e restituisce un Orario.
 */
public class ProssimaPartenzaDAO {

    public Orario prossimaPartenza(int numeroTratta, char direzione) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.CONDUCENTE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op02_prossima_partenza(?, ?)}")) {

            cs.setInt(1, numeroTratta);
            cs.setString(2, String.valueOf(direzione));

            boolean hasRs = cs.execute();
            if (!hasRs) {
                throw new DatabaseException("Errore 0: \"OP02: nessun risultato restituito dalla stored procedure.\"");
            }

            try (ResultSet rs = cs.getResultSet()) {
                if (!rs.next()) {
                    throw new DatabaseException("Errore 0: \"OP02: result set vuoto.\"");
                }

                Time t = rs.getTime("prossima_partenza");
                if (t == null) {
                    throw new DatabaseException("Errore 0: \"OP02: campo prossima_partenza NULL.\"");
                }

                return new Orario(t.toLocalTime());
            }

        } catch (SQLException e) {
            throw new DatabaseException(
                    String.format("Errore %d: \"%s\"", e.getErrorCode(), safeSqlMessage(e)),
                    e
            );
        }
    }

    private String safeSqlMessage(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return "(nessun messaggio)";
        return msg.replace("\n", " ").replace("\r", " ");
    }
}