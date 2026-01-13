package model;

/**
 * OP12 – Avanza fermata.
 * Rappresenta l'effetto dell'operazione "avanza fermata" su un veicolo:
 * - su quale veicolo è stata eseguita (matricola)
 * - su quale tratta/direzione sta viaggiando
 * - fermata precedente e fermata nuova (aggiornamento della tabella Fa)
 */
public class SpostamentoFermata {

    private final String matricola;
    private final int numeroTratta;
    private final char direzione;
    private final String fermataPrecedente;
    private final String fermataNuova;

    public SpostamentoFermata(String matricola,
                              int numeroTratta,
                              char direzione,
                              String fermataPrecedente,
                              String fermataNuova) {
        this.matricola = matricola;
        this.numeroTratta = numeroTratta;
        this.direzione = direzione;
        this.fermataPrecedente = fermataPrecedente;
        this.fermataNuova = fermataNuova;
    }

    public String getMatricola() { return matricola; }
    public int getNumeroTratta() { return numeroTratta; }
    public char getDirezione() { return direzione; }
    public String getFermataPrecedente() { return fermataPrecedente; }
    public String getFermataNuova() { return fermataNuova; }
}