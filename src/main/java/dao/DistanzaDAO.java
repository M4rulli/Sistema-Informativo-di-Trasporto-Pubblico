package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DistanzaDAO {

    /**
     * OP01 – distanza in numero di fermate.
     * @return distanza (ordine(target) - ordine(corrente))
     */
    public int distanzaFermate(String matricola, String codFermataTarget) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.UTENTE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op01_distanza_fermate(?, ?)}")) {

            cs.setString(1, matricola);
            cs.setString(2, codFermataTarget);

            boolean hasResultSet = cs.execute();
            if (!hasResultSet) {
                throw new DatabaseException("Nessun risultato restituito dalla stored procedure.");
            }

            try (ResultSet rs = cs.getResultSet()) {
                if (!rs.next()) {
                    throw new DatabaseException("Result set vuoto.");
                }
                return rs.getInt("distanza_fermate");
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
