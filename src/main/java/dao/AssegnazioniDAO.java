package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;

public class AssegnazioniDAO {

    public void assegnaVeicoloATratta(String matricola, int numeroTratta, char direzione) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op04_assegna_veicolo_tratta(?, ?, ?)}")) {

            cs.setString(1, matricola);
            cs.setInt(2, numeroTratta);
            cs.setString(3, String.valueOf(direzione));
            cs.execute();

        } catch (SQLException e) {
            throw new DatabaseException(String.format("Errore %d: \"%s\"", e.getErrorCode(), safeSqlMessage(e)), e);
        }
    }

    public void assegnaConducenteAVeicolo(String cf, String matricola) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op05_assegna_conducente_veicolo(?, ?)}")) {

            cs.setString(1, cf);
            cs.setString(2, matricola);
            cs.execute();

        } catch (SQLException e) {
            throw new DatabaseException(String.format("Errore %d: \"%s\"", e.getErrorCode(), safeSqlMessage(e)), e);
        }
    }

    public int aggiungiFermataATratta(int numeroTratta, char direzione, String codFermata) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op11_aggiungi_fermata_tratta(?, ?, ?)}")) {

            cs.setInt(1, numeroTratta);
            cs.setString(2, String.valueOf(direzione));
            cs.setString(3, codFermata);

            cs.execute();

            try (ResultSet rs = cs.getResultSet()) {
                rs.next();
                return rs.getInt("ordine");
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