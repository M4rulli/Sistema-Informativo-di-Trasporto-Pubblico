package dao;

import exceptions.DatabaseException;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;

public class VeicoliDAO {

    public void veicolo(String azione, String matricola, LocalDate dataAcquisto) throws DatabaseException {
        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.GESTORE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op09_veicolo(?, ?, ?)}")) {

            cs.setString(1, azione);
            cs.setString(2, matricola);

            if (dataAcquisto == null) cs.setNull(3, Types.DATE);
            else cs.setDate(3, Date.valueOf(dataAcquisto));

            cs.execute();

        } catch (SQLException e) {
            throw new DatabaseException(e.getSQLState(), e);
        }
    }
}