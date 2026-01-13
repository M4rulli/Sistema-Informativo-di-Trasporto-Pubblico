package controller;

import dao.AvanzaFermataDAO;
import dao.ProssimaPartenzaDAO;
import dao.ProssimeFermateDAO;
import exceptions.DatabaseException;
import model.Orario;
import view.ConducenteView;

import java.util.List;

/**
 * Controller applicativo del ruolo CONDUCENTE.
 * Orchestrazione: input (View) -> chiamata SP tramite DAO -> output (View).
 */
public class ConducenteController {

    private final ConducenteView view;
    private final ProssimaPartenzaDAO prossimaPartenzaDAO;
    private final AvanzaFermataDAO avanzaFermataDAO;
    private final ProssimeFermateDAO prossimeFermateDAO;

    public ConducenteController(ConducenteView view) {
        this.view = view;
        this.prossimaPartenzaDAO = new ProssimaPartenzaDAO();
        this.avanzaFermataDAO = new AvanzaFermataDAO();
        this.prossimeFermateDAO = new ProssimeFermateDAO();
    }

    /**
     * Loop del menu conducente. Ritorna quando l’utente sceglie “Indietro”.
     */
    public void run() {
        boolean running = true;

        while (running) {
            ConducenteView.Choice choice = view.showMenu();

            switch (choice) {
                case OP02_PROSSIMA_PARTENZA -> handleOp02();
                case OP12_AVANZA -> handleOp12();
                case OP13_PROSSIME -> handleOp13();
                case INDIETRO -> running = false;
            }
        }
    }

    private void handleOp02() {
        String sNumero = view.askNumeroTratta();
        if (isBack(sNumero)) return;

        Integer numeroTratta = parseIntOrNull(sNumero);
        if (numeroTratta == null) {
            view.showErrorMessage("Numero tratta non valido.");
            return;
        }

        String sDir = view.askDirezione();
        if (isBack(sDir)) return;

        Character dir = parseDirezioneOrNull(sDir);
        if (dir == null) {
            view.showErrorMessage("Direzione non valida (usa A oppure R).");
            return;
        }

        try {
            Orario next = prossimaPartenzaDAO.prossimaPartenza(numeroTratta, dir);
            view.showOp02Result(numeroTratta, dir, next);
        } catch (DatabaseException e) {
            view.showErrorMessage(e.getMessage());
        }
    }

    private void handleOp12() {
        String matricola = view.askMatricola();
        if (isBack(matricola)) return;

        if (matricola.isBlank()) {
            view.showErrorMessage("Matricola obbligatoria.");
            return;
        }

        try {
            // La stored procedure OP12 restituisce un result set con le informazioni di avanzamento.
            // Il DAO espone un model (SpostamentoFermata), ma la View attuale vuole i campi separati.
            var res = avanzaFermataDAO.avanza(matricola);
            view.showOp12Result(
                    res.getMatricola(),
                    res.getNumeroTratta(),
                    res.getDirezione(),
                    res.getFermataPrecedente(),
                    res.getFermataNuova()
            );
        } catch (DatabaseException e) {
            view.showErrorMessage(e.getMessage());
        }
    }

    private void handleOp13() {
        String matricola = view.askMatricola();
        if (isBack(matricola)) return;

        if (matricola.isBlank()) {
            view.showErrorMessage("Matricola obbligatoria.");
            return;
        }

        String sN = view.askN();
        if (isBack(sN)) return;

        Integer n = parseIntOrNull(sN);
        if (n == null || n <= 0) {
            view.showErrorMessage("N non valido (deve essere un intero > 0).");
            return;
        }

        try {
            // OP13 nel DB restituisce cod_fermata e ordine (ma nel client possiamo mostrare solo i codici).
            // Il DAO restituisce una lista di FermataInSequenza; qui estraiamo i codici.
            var list = prossimeFermateDAO.prossime(matricola, n);

            List<String> codici = new java.util.ArrayList<>();
            if (list != null) {
                for (var f : list) {
                    codici.add(f.codFermata());
                }
            }

            view.showOp13Result(matricola, codici);
        } catch (DatabaseException e) {
            view.showErrorMessage(e.getMessage());
        }
    }

    // -------------------------
    // Helpers
    // -------------------------

    private boolean isBack(String s) {
        return s == null || s.equalsIgnoreCase("/back");
    }

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Character parseDirezioneOrNull(String s) {
        if (s == null) return null;
        String t = s.trim().toUpperCase();
        if (t.length() != 1) return null;
        char c = t.charAt(0);
        if (c != 'A' && c != 'R') return null;
        return c;
    }
}