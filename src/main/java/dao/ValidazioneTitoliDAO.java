package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;

public class ValidazioneTitoliDAO {

    public record EsitoBiglietto(String codTitolo, LocalDate dataValidazione) {}
    public record EsitoAbbonamento(String codTitolo, LocalDate scadenza) {}

    public EsitoBiglietto validaBiglietto(String codTitolo) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op03a_validazione_biglietto(?)}")) {

            cs.setString(1, codTitolo);
            cs.execute();

            try (ResultSet rs = cs.getResultSet()) {
                rs.next();
                return new EsitoBiglietto(rs.getString("cod_titolo"),
                        rs.getDate("data_validazione").toLocalDate());
            }

        } catch (SQLException e) {
            throw new DatabaseException(String.format("Errore %d: \"%s\"", e.getErrorCode(), safeSqlMessage(e)), e);
        }
    }

    public EsitoAbbonamento validaAbbonamento(String codTitolo) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op03b_validazione_abbonamento(?)}")) {

            cs.setString(1, codTitolo);
            cs.execute();

            try (ResultSet rs = cs.getResultSet()) {
                rs.next();
                return new EsitoAbbonamento(rs.getString("cod_titolo"),
                        rs.getDate("scadenza").toLocalDate());
            }

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