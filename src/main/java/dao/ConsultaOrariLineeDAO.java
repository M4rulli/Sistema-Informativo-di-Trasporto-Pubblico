package dao;

import exceptions.DatabaseException;
import model.Fermata;
import model.Orario;
import model.Role;
import pattern.ConnectionFactory;

import java.sql.*;
import java.time.LocalTime;
import java.util.List;

public class ConsultaOrariLineeDAO {

    /**
     * OP14: restituisce 2 result set:
     *  RS#1 -> fermate (ordine, cod_fermata, lat, lon)
     *  RS#2 -> orari (ora_partenza)
     * Riempie due liste di model (riusabili) senza creare un "result model".
     */
    public void consulta(int numeroTratta,
                         String direzione,
                         List<Fermata> outFermate,
                         List<Orario> outOrari) throws DatabaseException {

        if (outFermate == null || outOrari == null) {
            throw new IllegalArgumentException("Le liste di output non possono essere null.");
        }

        outFermate.clear();
        outOrari.clear();

        try (Connection conn = ConnectionFactory.getConnectionForRole(Role.UTENTE);
             CallableStatement cs = conn.prepareCall("{CALL sp_op14_consulta_orari(?, ?)}")) {

            cs.setInt(1, numeroTratta);
            cs.setString(2, direzione);

            boolean hasFirst = cs.execute();
            if (!hasFirst) {
                throw new DatabaseException("OP14 - Errore (0): \"Nessun result set restituito (RS#1 mancante)\".");
            }

            // -----------------------
            // RS#1: fermate
            // -----------------------
            try (ResultSet rs1 = cs.getResultSet()) {
                while (rs1.next()) {
                    int ordine = rs1.getInt("ordine");
                    String cod = rs1.getString("cod_fermata");
                    double lat = rs1.getDouble("lat");
                    double lon = rs1.getDouble("lon");

                    outFermate.add(new Fermata(cod, lat, lon, ordine));
                }
            }

            // -----------------------
            // RS#2: orari
            // -----------------------
            boolean hasSecond = cs.getMoreResults();
            if (!hasSecond) {
                throw new DatabaseException("OP14 - Errore (0): \"Secondo result set (orari) non trovato (RS#2 mancante)\".");
            }

            try (ResultSet rs2 = cs.getResultSet()) {
                while (rs2.next()) {
                    Time t = rs2.getTime("ora_partenza");
                    LocalTime ora = (t != null) ? t.toLocalTime() : null;
                    outOrari.add(new Orario(ora));
                }
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