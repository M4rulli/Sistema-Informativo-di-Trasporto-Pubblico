package dao;

import exceptions.DatabaseException;
import model.FermataInSequenza;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO OP13 – Prossime N fermate.
 * Chiama sp_op13_prossime_fermate(matricola, N)
 * e restituisce una lista di FermataInSequenza (codFermata, ordine).
 */
public class ProssimeFermateDAO {

    public List<FermataInSequenza> prossime(String matricola, int n) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.CONDUCENTE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op13_prossime_fermate(?, ?)}")) {

            cs.setString(1, matricola);
            cs.setInt(2, n);

            boolean hasRs = cs.execute();
            if (!hasRs) {
                throw new DatabaseException("Errore 0: \"OP13: nessun risultato restituito dalla stored procedure.\"");
            }

            List<FermataInSequenza> out = new ArrayList<>();

            try (ResultSet rs = cs.getResultSet()) {
                while (rs.next()) {
                    String cod = rs.getString("cod_fermata");
                    int ordine = rs.getInt("ordine");
                    out.add(new FermataInSequenza(cod, ordine));
                }
            }

            return out;

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