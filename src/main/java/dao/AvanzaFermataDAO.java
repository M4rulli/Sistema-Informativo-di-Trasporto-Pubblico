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

            while (!hasRs && cs.getUpdateCount() != -1) {
                hasRs = cs.getMoreResults();
            }

            if (!hasRs) {
                throw new DatabaseException("OP12: nessun ResultSet restituito dalla stored procedure.");
            }

            try (ResultSet rs = cs.getResultSet()) {
                if (rs == null) {
                    throw new DatabaseException("OP12: ResultSet nullo (nessun risultato).");
                }
                if (!rs.next()) {
                    throw new DatabaseException("OP12: ResultSet vuoto.");
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
            throw new DatabaseException(e.getSQLState(), e);
        }
    }
}