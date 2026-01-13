package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;
import java.time.LocalTime;

public class OrariDAO {

    public void crudOrario(String azione, int numeroTratta, char direzione,
                           LocalTime ora, LocalTime oraNew) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op07_orario_crud(?, ?, ?, ?, ?)}")) {

            cs.setString(1, azione);
            cs.setInt(2, numeroTratta);
            cs.setString(3, String.valueOf(direzione));
            cs.setTime(4, Time.valueOf(ora));

            if (oraNew == null) cs.setNull(5, Types.TIME);
            else cs.setTime(5, Time.valueOf(oraNew));

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