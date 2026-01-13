package controller;

import dao.*;
import exceptions.DatabaseException;
import view.GestoreView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class GestoreController {

    private final GestoreView view;

    private final ValidazioneTitoliDAO validazioneTitoliDAO;
    private final AssegnazioniDAO assegnazioniDAO;
    private final ConducentiDAO conducentiDAO;
    private final OrariDAO orariDAO;
    private final EmissioneTitoliDAO emissioneTitoliDAO;
    private final VeicoliDAO veicoliDAO;
    private final FermateDAO fermateDAO;

    public GestoreController(GestoreView view) {
        this.view = view;

        this.validazioneTitoliDAO = new ValidazioneTitoliDAO();
        this.assegnazioniDAO = new AssegnazioniDAO();
        this.conducentiDAO = new ConducentiDAO();
        this.orariDAO = new OrariDAO();
        this.emissioneTitoliDAO = new EmissioneTitoliDAO();
        this.veicoliDAO = new VeicoliDAO();
        this.fermateDAO = new FermateDAO();
    }

    public void run() {
        boolean running = true;

        while (running) {
            GestoreView.Choice choice = view.showMenu();

            try {
                switch (choice) {
                    case OP03A_BIGLIETTO -> handleOp03a();
                    case OP03B_ABBONAMENTO -> handleOp03b();
                    case OP04_ASSEGNA_VEICOLO_TRATTA -> handleOp04();
                    case OP05_ASSEGNA_CONDUCENTE_VEICOLO -> handleOp05();
                    case OP06_INSERISCI_CONDUCENTE -> handleOp06a();
                    case OP06_AGGIORNA_CONDUCENTE -> handleOp06b();
                    case OP07_ORARI_CRUD -> handleOp07();
                    case OP08A_EMETTI_BIGLIETTI_LOTTO -> handleOp08a();
                    case OP08B_EMETTI_ABBONAMENTO -> handleOp08b();
                    case OP09_VEICOLO -> handleOp09();
                    case OP10_FERMATA -> handleOp10();
                    case OP11_AGGIUNGI_FERMATA_TRATTA -> handleOp11();
                    case INDIETRO -> running = false;
                }
            } catch (DatabaseException e) {
                view.showErrorMessage(e.getMessage());
            } catch (Exception e) {
                view.showErrorMessage("Errore: " + e.getMessage());
            }
        }
    }

    // ---------------- OP03a ----------------
    private void handleOp03a() throws DatabaseException {
        String cod = view.ask("Codice biglietto (12 char)");
        if (cod.equalsIgnoreCase("/back")) return;
        if (cod.isBlank()) { view.showErrorMessage("Codice titolo obbligatorio."); return; }

        var res = validazioneTitoliDAO.validaBiglietto(cod);
        view.showOk("OP03a – Validazione biglietto",
                List.of("Esito: OK", "Titolo: " + res.codTitolo(), "Data validazione: " + res.dataValidazione()));
    }

    // ---------------- OP03b ----------------
    private void handleOp03b() throws DatabaseException {
        String cod = view.ask("Codice abbonamento (12 char)");
        if (cod.equalsIgnoreCase("/back")) return;
        if (cod.isBlank()) { view.showErrorMessage("Codice titolo obbligatorio."); return; }

        var res = validazioneTitoliDAO.validaAbbonamento(cod);
        view.showOk("OP03b – Validazione abbonamento",
                List.of("Esito: OK", "Titolo: " + res.codTitolo(), "Scadenza: " + res.scadenza()));
    }

    // ---------------- OP04 ----------------
    private void handleOp04() throws DatabaseException {
        String matr = view.ask("Matricola veicolo (es. A001)");
        if (matr.equalsIgnoreCase("/back")) return;
        String nt = view.ask("Numero tratta (es. 1)");
        if (nt.equalsIgnoreCase("/back")) return;
        String dir = view.ask("Direzione (A/R)");
        if (dir.equalsIgnoreCase("/back")) return;

        int numeroTratta = Integer.parseInt(nt);
        char direzione = dir.trim().toUpperCase().charAt(0);

        assegnazioniDAO.assegnaVeicoloATratta(matr, numeroTratta, direzione);
        view.showOk("OP04 – Assegnazione veicolo a tratta",
                List.of("Esito: OK", "Veicolo: " + matr, "Tratta: " + numeroTratta + " (" + direzione + ")"));
    }

    // ---------------- OP05 ----------------
    private void handleOp05() throws DatabaseException {
        String cf = view.ask("CF conducente (16 char)");
        if (cf.equalsIgnoreCase("/back")) return;
        String matr = view.ask("Matricola veicolo (es. A001)");
        if (matr.equalsIgnoreCase("/back")) return;

        assegnazioniDAO.assegnaConducenteAVeicolo(cf, matr);
        view.showOk("OP05 – Assegnazione conducente a veicolo",
                List.of("Esito: OK", "CF: " + cf, "Veicolo: " + matr));
    }

    // ---------------- OP06a ----------------
    private void handleOp06a() throws DatabaseException {
        String cf = view.ask("CF");
        if (cf.equalsIgnoreCase("/back")) return;

        String nome = view.ask("Nome");
        if (nome.equalsIgnoreCase("/back")) return;

        String cognome = view.ask("Cognome");
        if (cognome.equalsIgnoreCase("/back")) return;

        String dn = view.ask("Data nascita (YYYY-MM-DD)");
        if (dn.equalsIgnoreCase("/back")) return;

        String ln = view.ask("Luogo nascita");
        if (ln.equalsIgnoreCase("/back")) return;

        String pat = view.ask("Numero patente");
        if (pat.equalsIgnoreCase("/back")) return;

        String scad = view.ask("Scadenza patente (YYYY-MM-DD)");
        if (scad.equalsIgnoreCase("/back")) return;

        conducentiDAO.inserisciConducente(
                cf, nome, cognome,
                LocalDate.parse(dn),
                ln, pat,
                LocalDate.parse(scad)
        );

        view.showOk("OP06a – Inserisci conducente", List.of("Esito: OK", "CF: " + cf));
    }

    // ---------------- OP06b ----------------
    private void handleOp06b() throws DatabaseException {
        String cf = view.ask("CF (conducente da aggiornare)");
        if (cf.equalsIgnoreCase("/back")) return;

        // qui puoi inserire anche solo alcuni campi: lasci vuoto => NULL => COALESCE in SQL
        String nome = view.ask("Nuovo nome (lascia vuoto per non cambiare)");
        if (nome.equalsIgnoreCase("/back")) return;

        String cognome = view.ask("Nuovo cognome (lascia vuoto per non cambiare)");
        if (cognome.equalsIgnoreCase("/back")) return;

        String dn = view.ask("Nuova data nascita YYYY-MM-DD (vuoto=no)");
        if (dn.equalsIgnoreCase("/back")) return;

        String ln = view.ask("Nuovo luogo nascita (vuoto=no)");
        if (ln.equalsIgnoreCase("/back")) return;

        String pat = view.ask("Nuovo numero patente (vuoto=no)");
        if (pat.equalsIgnoreCase("/back")) return;

        String scad = view.ask("Nuova scadenza patente YYYY-MM-DD (vuoto=no)");
        if (scad.equalsIgnoreCase("/back")) return;

        conducentiDAO.aggiornaConducente(
                cf,
                blankToNull(nome),
                blankToNull(cognome),
                blankToNull(dn) == null ? null : LocalDate.parse(dn),
                blankToNull(ln),
                blankToNull(pat),
                blankToNull(scad) == null ? null : LocalDate.parse(scad)
        );

        view.showOk("OP06b – Aggiorna conducente", List.of("Esito: OK", "CF: " + cf));
    }

    // ---------------- OP07 ----------------
    private void handleOp07() throws DatabaseException {
        String az = view.ask("Azione orari (INS/DEL/UPD)");
        if (az.equalsIgnoreCase("/back")) return;
        az = az.trim().toUpperCase();

        String nt = view.ask("Numero tratta");
        if (nt.equalsIgnoreCase("/back")) return;
        int numeroTratta = Integer.parseInt(nt);

        String dir = view.ask("Direzione (A/R)");
        if (dir.equalsIgnoreCase("/back")) return;
        char direzione = dir.trim().toUpperCase().charAt(0);

        String ora = view.ask("Ora (HH:MM) (per INS/DEL/UPD)");
        if (ora.equalsIgnoreCase("/back")) return;

        String oraNew = "";
        if (az.equals("UPD")) {
            oraNew = view.ask("Nuova ora (HH:MM)");
            if (oraNew.equalsIgnoreCase("/back")) return;
        }

        LocalTime tOra = LocalTime.parse(ora.length()==5 ? ora : ora + ":00");
        LocalTime tOraNew = blankToNull(oraNew) == null ? null : LocalTime.parse(oraNew.length()==5 ? oraNew : oraNew + ":00");

        orariDAO.crudOrario(az, numeroTratta, direzione, tOra, tOraNew);
        view.showOk("OP07 – Gestione orari", List.of("Esito: OK", "Azione: " + az));
    }

    // ---------------- OP08a ----------------
    private void handleOp08a() throws DatabaseException {
        String q = view.ask("Quantità biglietti (es. 50)");
        if (q.equalsIgnoreCase("/back")) return;
        int quantita = Integer.parseInt(q);

        var res = emissioneTitoliDAO.emettiBigliettiLotto(quantita);
        view.showOk("OP08a – Emissione biglietti lotto",
                List.of("Esito: OK", "Emessi: " + res.quantita(), "Prefisso lotto: " + res.prefisso()));
    }

    // ---------------- OP08b ----------------
    private void handleOp08b() throws DatabaseException {
        String cod = view.ask("Codice abbonamento (12 char)");
        if (cod.equalsIgnoreCase("/back")) return;

        String scad = view.ask("Scadenza (YYYY-MM-DD)");
        if (scad.equalsIgnoreCase("/back")) return;

        emissioneTitoliDAO.emettiAbbonamento(cod, LocalDate.parse(scad));
        view.showOk("OP08b – Emissione abbonamento",
                List.of("Esito: OK", "Titolo: " + cod, "Scadenza: " + scad));
    }

    // ---------------- OP09 ----------------
    private void handleOp09() throws DatabaseException {
        String az = view.ask("Azione veicolo (INS/UPD)");
        if (az.equalsIgnoreCase("/back")) return;
        az = az.trim().toUpperCase();

        String matr = view.ask("Matricola (4 char)");
        if (matr.equalsIgnoreCase("/back")) return;

        String da = view.ask("Data acquisto (YYYY-MM-DD) (obbligatoria per INS, opzionale per UPD)");
        if (da.equalsIgnoreCase("/back")) return;

        LocalDate data = blankToNull(da) == null ? null : LocalDate.parse(da);

        veicoliDAO.veicolo(az, matr, data);
        view.showOk("OP09 – Gestione veicoli", List.of("Esito: OK", "Azione: " + az, "Matricola: " + matr));
    }

    // ---------------- OP10 ----------------
    private void handleOp10() throws DatabaseException {
        String az = view.ask("Azione fermata (INS/UPD)");
        if (az.equalsIgnoreCase("/back")) return;
        az = az.trim().toUpperCase();

        String cod = view.ask("Codice fermata (es. F001)");
        if (cod.equalsIgnoreCase("/back")) return;

        String lat = view.ask("Latitudine (es. 41.900000) (obbligatoria in INS)");
        if (lat.equalsIgnoreCase("/back")) return;

        String lon = view.ask("Longitudine (es. 12.500000) (obbligatoria in INS)");
        if (lon.equalsIgnoreCase("/back")) return;

        Double dLat = blankToNull(lat) == null ? null : Double.parseDouble(lat);
        Double dLon = blankToNull(lon) == null ? null : Double.parseDouble(lon);

        fermateDAO.fermata(az, cod, dLat, dLon);
        view.showOk("OP10 – Gestione fermate", List.of("Esito: OK", "Azione: " + az, "Fermata: " + cod));
    }

    // ---------------- OP11 ----------------
    private void handleOp11() throws DatabaseException {
        String nt = view.ask("Numero tratta");
        if (nt.equalsIgnoreCase("/back")) return;
        int numeroTratta = Integer.parseInt(nt);

        String dir = view.ask("Direzione (A/R)");
        if (dir.equalsIgnoreCase("/back")) return;
        char direzione = dir.trim().toUpperCase().charAt(0);

        String cod = view.ask("Codice fermata da aggiungere (es. F010)");
        if (cod.equalsIgnoreCase("/back")) return;

        int ordine = assegnazioniDAO.aggiungiFermataATratta(numeroTratta, direzione, cod);
        view.showOk("OP11 – Aggiungi fermata a tratta",
                List.of("Esito: OK", "Tratta: " + numeroTratta + " (" + direzione + ")", "Fermata: " + cod, "Ordine assegnato: " + ordine));
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}