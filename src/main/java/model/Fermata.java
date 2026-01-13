package model;

public class Fermata {

    private final String codFermata;
    private final double lat;
    private final double lon;
    private final int ordine;

    public Fermata(String codFermata, double lat, double lon, int ordine) {
        this.codFermata = codFermata;
        this.lat = lat;
        this.lon = lon;
        this.ordine = ordine;
    }

    public String getCodFermata() {
        return codFermata;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getOrdine() {
        return ordine;
    }

    @Override
    public String toString() {
        return ordine + ") " + codFermata + " (" + lat + ", " + lon + ")";
    }
}