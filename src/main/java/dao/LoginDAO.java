package dao;

import exceptions.DatabaseException;
import model.LoginCredentials;
import model.LoginResult;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class LoginDAO {

    private final LoginCredentials credentials;

    public LoginDAO(LoginCredentials credentials) {
        this.credentials = credentials;
    }

    public LoginResult execute() throws DatabaseException {
        try {
            try (Connection conn = ConnectionFactory.getConnectionForLogin();
                 CallableStatement cs = conn.prepareCall("{CALL trasporto_pubblico.sp_login(?, ?, ?, ?)}")) {

                cs.setString(1, credentials.getUsername());
                cs.setString(2, credentials.getPassword());

                // OUT: ruolo + (opzionale) cf_conducente
                cs.registerOutParameter(3, Types.VARCHAR);
                cs.registerOutParameter(4, Types.VARCHAR);

                cs.execute();

                String ruoloStr = cs.getString(3);
                if (ruoloStr == null || ruoloStr.isBlank()) {
                    return null; // login fallito
                }

                Role ruolo = Role.valueOf(ruoloStr);
                String cf = cs.getString(4); // può essere null

                return new LoginResult(credentials.getUsername(), ruolo, cf);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Errore durante il tentativo di login.", e);
        }
    }
}