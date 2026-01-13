package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;

public class FermateDAO {

    public void fermata(String azione, String codFermata, Double lat, Double lon) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op10_fermata(?, ?, ?, ?)}")) {

            cs.setString(1, azione);
            cs.setString(2, codFermata);

            if (lat == null) cs.setNull(3, Types.DECIMAL);
            else cs.setDouble(3, lat);

            if (lon == null) cs.setNull(4, Types.DECIMAL);
            else cs.setDouble(4, lon);

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