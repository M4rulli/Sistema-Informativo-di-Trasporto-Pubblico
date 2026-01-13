package view;

import model.Orario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConducenteView extends BaseCliView {

    public enum Choice {
        OP12_AVANZA,
        OP13_PROSSIME,
        OP02_PROSSIMA_PARTENZA,
        INDIETRO
    }

    public Choice showMenu() {
        int c = chooseBox(
                "Menu Conducente",
                List.of(
                        "Avanza alla fermata successiva (OP12)",
                        "Mostra prossime N fermate (OP13)",
                        "Prossima partenza capolinea (OP02)",
                        "Indietro"
                )
        );

        return switch (c) {
            case 1 -> Choice.OP12_AVANZA;
            case 2 -> Choice.OP13_PROSSIME;
            case 3 -> Choice.OP02_PROSSIMA_PARTENZA;
            default -> Choice.INDIETRO;
        };
    }

    // ---------- INPUT ----------
    public String askMatricola() {
        System.out.println();
        System.out.print("Matricola veicolo (es. A123) oppure /back: ");
        return readLineSafe();
    }

    public String askN() {
        System.out.print("Quante fermate vuoi vedere? (N) oppure /back: ");
        return readLineSafe();
    }

    public String askNumeroTratta() {
        System.out.println();
        System.out.print("Numero tratta (es. 12) oppure /back: ");
        return readLineSafe();
    }

    public String askDirezione() {
        System.out.print("Direzione (A/R) oppure /back: ");
        return readLineSafe();
    }

    // ---------- OUTPUT ----------
    public void showOp12Result(String matricola, int numeroTratta, char direzione, String prev, String next) {
        List<String> lines = new ArrayList<>();
        lines.add("Matricola: " + matricola);
        lines.add("Tratta: " + numeroTratta + " (" + direzione + ")");
        lines.add("");
        lines.add("Fermata precedente: " + prev);
        lines.add("Fermata nuova: " + next);
        lines.add("");
        lines.add("OK: il veicolo e' avanzato di una fermata.");

        printBox("OP12 – Avanza fermata", lines, 80);
    }

    public void showOp13Result(String matricola, List<String> codFermate) {
        List<String> lines = new ArrayList<>();
        lines.add("Matricola: " + matricola);
        lines.add("");

        if (codFermate == null || codFermate.isEmpty()) {
            lines.add("Nessuna fermata restituita.");
        } else {
            lines.add("Prossime fermate:");
            for (int i = 0; i < codFermate.size(); i++) {
                lines.add((i + 1) + ") " + codFermate.get(i));
            }
        }

        printBox("OP13 – Prossime N fermate", lines, 80);
    }

    public void showOp02Result(int numeroTratta, char direzione, Orario orario) {
        List<String> lines = new ArrayList<>();
        lines.add("Tratta: " + numeroTratta + " (" + direzione + ")");
        lines.add("");

        if (orario == null) {
            lines.add("Prossima partenza: (n/a)");
        } else {
            lines.add("Prossima partenza: " + orario.toHHMM());
        }

        printBox("OP02 – Prossima partenza", lines, 70);
    }


    public void showErrorMessage(String message) {
        printBox("Errore", List.of(message), 90);
    }

    private String readLineSafe() {
        try {
            String s = reader.readLine();
            return (s == null) ? "" : s.trim();
        } catch (IOException e) {
            return "";
        }
    }
}