package model;

import java.time.LocalTime;

public class Orario {

    private final LocalTime oraPartenza;

    public Orario(LocalTime oraPartenza) {
        this.oraPartenza = oraPartenza;
    }

    public LocalTime getOraPartenza() {
        return oraPartenza;
    }

    /**
     * Formato “HH:MM” comodo per CLI.
     */
    public String toHHMM() {
        if (oraPartenza == null) return "";
        return String.format("%02d:%02d", oraPartenza.getHour(), oraPartenza.getMinute());
    }

    @Override
    public String toString() {
        return toHHMM();
    }
}