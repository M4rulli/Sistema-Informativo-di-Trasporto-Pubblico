package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;

public class EmissioneTitoliDAO {

    public record LottoResult(int quantita, String prefisso) {}

    public LottoResult emettiBigliettiLotto(int quantita) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op08a_emetti_biglietti_lotto(?)}")) {

            cs.setInt(1, quantita);
            cs.execute();

            try (ResultSet rs = cs.getResultSet()) {
                rs.next();
                return new LottoResult(rs.getInt("biglietti_emessi"), rs.getString("lotto_prefix"));
            }

        } catch (SQLException e) {
            throw new DatabaseException(String.format("Errore %d: \"%s\"", e.getErrorCode(), safeSqlMessage(e)), e);
        }
    }

    public void emettiAbbonamento(String codTitolo, LocalDate scadenza) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op08b_emetti_abbonamento(?, ?)}")) {

            cs.setString(1, codTitolo);
            cs.setDate(2, Date.valueOf(scadenza));
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