package view;

import java.io.IOException;
import java.util.List;

public class GestoreView extends BaseCliView {

    public enum Choice {
        OP03A_BIGLIETTO,
        OP03B_ABBONAMENTO,
        OP04_ASSEGNA_VEICOLO_TRATTA,
        OP05_ASSEGNA_CONDUCENTE_VEICOLO,
        OP06_INSERISCI_CONDUCENTE,
        OP06_AGGIORNA_CONDUCENTE,
        OP07_ORARI_CRUD,
        OP08A_EMETTI_BIGLIETTI_LOTTO,
        OP08B_EMETTI_ABBONAMENTO,
        OP09_VEICOLO,
        OP10_FERMATA,
        OP11_AGGIUNGI_FERMATA_TRATTA,
        INDIETRO
    }

    public Choice showMenu() {
        int c = chooseBox(
                "Menu Gestore",
                List.of(
                        "Valida biglietto (OP03a)",
                        "Valida abbonamento (OP03b)",
                        "Assegna veicolo a tratta (OP04)",
                        "Assegna conducente a veicolo (OP05)",
                        "Inserisci conducente (OP06a)",
                        "Aggiorna conducente (OP06b)",
                        "Gestione orari (OP07)",
                        "Emetti biglietti in lotto (OP08a)",
                        "Emetti abbonamento (OP08b)",
                        "Gestione veicoli (OP09)",
                        "Gestione fermate (OP10)",
                        "Aggiungi fermata a tratta (OP11)",
                        "Indietro"
                )
        );

        return switch (c) {
            case 1 -> Choice.OP03A_BIGLIETTO;
            case 2 -> Choice.OP03B_ABBONAMENTO;
            case 3 -> Choice.OP04_ASSEGNA_VEICOLO_TRATTA;
            case 4 -> Choice.OP05_ASSEGNA_CONDUCENTE_VEICOLO;
            case 5 -> Choice.OP06_INSERISCI_CONDUCENTE;
            case 6 -> Choice.OP06_AGGIORNA_CONDUCENTE;
            case 7 -> Choice.OP07_ORARI_CRUD;
            case 8 -> Choice.OP08A_EMETTI_BIGLIETTI_LOTTO;
            case 9 -> Choice.OP08B_EMETTI_ABBONAMENTO;
            case 10 -> Choice.OP09_VEICOLO;
            case 11 -> Choice.OP10_FERMATA;
            case 12 -> Choice.OP11_AGGIUNGI_FERMATA_TRATTA;
            default -> Choice.INDIETRO;
        };
    }

    // ---------- INPUT GENERICI ----------
    public String ask(String prompt) {
        System.out.print(prompt + " oppure /back: ");
        return readLineSafe();
    }

    private String readLineSafe() {
        try {
            String s = reader.readLine();
            return (s == null) ? "" : s.trim();
        } catch (IOException e) {
            return "";
        }
    }

    // ---------- OUTPUT ----------
    public void showOk(String title, List<String> lines) {
        printBox(title, lines, 90);
    }

    public void showErrorMessage(String message) {
        printBox("Errore", List.of(message), 90);
    }
}