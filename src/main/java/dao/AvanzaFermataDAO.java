package dao;

import exceptions.DatabaseException;
import model.Role;
import model.SpostamentoFermata;
import pattern.ConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO OP12 – Avanza fermata.
 * Chiama sp_op12_avanza_fermata(matricola)
 * e restituisce un oggetto SpostamentoFermata con i dati del risultato.
 */
public class AvanzaFermataDAO {

    public SpostamentoFermata avanza(String matricola) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.CONDUCENTE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op12_avanza_fermata(?)}")) {

            cs.setString(1, matricola);

            boolean hasRs = cs.execute();
            if (!hasRs) {
                throw new DatabaseException("Errore 0: \"OP12: nessun risultato restituito dalla stored procedure.\"");
            }

            try (ResultSet rs = cs.getResultSet()) {
                if (!rs.next()) {
                    throw new DatabaseException("Errore 0: \"OP12: result set vuoto.\"");
                }

                String outMatricola = rs.getString("matricola");
                int numTratta = rs.getInt("numero_tratta");
                String dirStr = rs.getString("direzione");
                String prev = rs.getString("fermata_precedente");
                String next = rs.getString("fermata_nuova");

                char dir = (dirStr == null || dirStr.isBlank()) ? '?' : dirStr.trim().charAt(0);

                return new SpostamentoFermata(outMatricola, numTratta, dir, prev, next);
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