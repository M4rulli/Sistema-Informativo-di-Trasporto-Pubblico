package controller;

import dao.DistanzaDAO;
import exceptions.DatabaseException;
import view.PublicView;
import dao.ConsultaOrariLineeDAO;
import model.Fermata;
import model.Orario;

import java.util.ArrayList;
import java.util.List;

public class PublicController {

    private final PublicView view;
    private final DistanzaDAO distanzaDAO;
    private final ConsultaOrariLineeDAO consultaOrariLineeDAO;

    public PublicController(PublicView view) {
        this.view = view;
        this.distanzaDAO = new DistanzaDAO();
        this.consultaOrariLineeDAO = new ConsultaOrariLineeDAO();
    }

    /**
     * Loop del menu pubblico\. Ritorna quando l’utente sceglie “Indietro”.
     */
    public void run() {
        boolean running = true;

        while (running) {
            PublicView.Choice choice = view.showMenu();

            switch (choice) {
                case OP01_DISTANZA -> handleOp01();
                case OP14_ORARI -> handleOp14();
                case INDIETRO -> running = false;
            }
        }
    }

    private void handleOp01() {
        String matricola = view.askMatricola();
        if (matricola.equalsIgnoreCase("/back")) return;
        if (matricola.isBlank()) {
            view.showErrorMessage("Matricola obbligatoria.");
            return;
        }

        String target = view.askFermataTarget();
        if (target.equalsIgnoreCase("/back")) return;
        if (target.isBlank()) {
            view.showErrorMessage("Codice fermata target obbligatorio.");
            return;
        }

        try {
            int distanza = distanzaDAO.distanzaFermate(matricola, target);
            view.showOp01Result(matricola, target, distanza);
        } catch (DatabaseException e) {
            view.showErrorMessage(e.getMessage());
        }
    }

    private void handleOp14() {
        String sNumeroTratta = view.askNumeroTratta();
        if (sNumeroTratta == null) return;
        if (sNumeroTratta.equalsIgnoreCase("/back")) return;
        if (sNumeroTratta.isBlank()) {
            view.showErrorMessage("Numero tratta obbligatorio.");
            return;
        }

        final int numeroTratta;
        try {
            numeroTratta = Integer.parseInt(sNumeroTratta.trim());
        } catch (NumberFormatException e) {
            view.showErrorMessage("Numero tratta non valido.");
            return;
        }

        String direzione = view.askDirezione();
        if (direzione == null) return;
        if (direzione.equalsIgnoreCase("/back")) return;
        direzione = direzione.trim().toUpperCase();

        if (direzione.length() != 1 || (!direzione.equals("A") && !direzione.equals("R"))) {
            view.showErrorMessage("Direzione non valida (usa A oppure R).");
            return;
        }

        try {
            List<Fermata> fermate = new ArrayList<>();
            List<Orario> orari = new ArrayList<>();

            // La DAO riempie le due liste leggendo i due result set della stored procedure OP14
            consultaOrariLineeDAO.consulta(numeroTratta, direzione, fermate, orari);

            view.showOp14Result(numeroTratta, direzione.charAt(0), fermate, orari);

        } catch (DatabaseException e) {
            view.showErrorMessage(e.getMessage());
        }
    }
}