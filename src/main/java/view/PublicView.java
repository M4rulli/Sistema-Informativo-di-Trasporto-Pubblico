package view;

import model.Fermata;
import model.Orario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PublicView extends BaseCliView {

    public enum Choice {
        OP01_DISTANZA,
        OP14_ORARI,
        INDIETRO
    }

    public Choice showMenu() {
        int c = chooseBox(
                "Consultazione pubblica",
                List.of(
                        "Distanza in fermate (OP01)",
                        "Consulta linea e orari (OP14)",
                        "Indietro"
                )
        );

        return switch (c) {
            case 1 -> Choice.OP01_DISTANZA;
            case 2 -> Choice.OP14_ORARI;
            default -> Choice.INDIETRO;
        };
    }

    // =========================
    // OP01
    // =========================

    public String askMatricola() {
        System.out.println();
        System.out.print("Matricola veicolo (es. A123) oppure /back: ");
        return readLineSafe();
    }

    public String askFermataTarget() {
        System.out.print("Codice fermata target (es. F010) oppure /back: ");
        return readLineSafe();
    }

    public void showOp01Result(String matricola, String target, int distanza) {
        List<String> lines = new ArrayList<>();
        lines.add("Matricola: " + matricola);
        lines.add("Fermata target: " + target);
        lines.add("");

        String frase;
        if (distanza > 0) {
            frase = "Mancano " + distanza + " fermate per arrivare alla fermata " + target + ".";
            lines.add("Risultato: +" + distanza + " (in avanti)");
        } else if (distanza < 0) {
            frase = "La fermata " + target + " e' " + (-distanza) + " fermate prima rispetto alla posizione attuale.";
            lines.add("Risultato: " + distanza + " (indietro)");
        } else {
            frase = "Sei gia' alla fermata " + target + ".";
            lines.add("Risultato: 0 (sei gia' alla target)");
        }

        lines.add("");
        lines.add(frase);

        printBox("OP01 – Distanza in fermate", lines, 90);
    }

    // =========================
    // OP14
    // =========================

    public String askNumeroTratta() {
        System.out.println();
        System.out.print("Numero tratta (es. 12) oppure /back: ");
        return readLineSafe();
    }

    public String askDirezione() {
        System.out.print("Direzione (A/R) oppure /back: ");
        return readLineSafe();
    }

    public void showOp14Result(int numeroTratta, char direzione, List<Fermata> fermate, List<Orario> orari) {
        List<String> lines = new ArrayList<>();
        lines.add("Linea: " + numeroTratta + " (" + direzione + ")");
        lines.add("");

        lines.add("Fermate (in ordine):");
        if (fermate == null || fermate.isEmpty()) {
            lines.add("- (nessuna fermata configurata)");
        } else {
            for (Fermata f : fermate) {
                // formato compatto: ordine) COD (lat, lon)
                lines.add(String.format("%d) %s (%.6f, %.6f)",
                        f.getOrdine(),
                        f.getCodFermata(),
                        f.getLat(),
                        f.getLon()
                ));
            }
        }

        lines.add("");
        lines.add("Orari di partenza:");
        if (orari == null || orari.isEmpty()) {
            lines.add("- (nessun orario configurato)");
        } else {
            for (Orario o : orari) {
                lines.add("- " + o.getOraPartenza());
            }
        }

        lines.add("");
        int nF = (fermate == null) ? 0 : fermate.size();
        int nO = (orari == null) ? 0 : orari.size();
        lines.add("In totale: " + nF + " fermate, " + nO + " orari.");

        printBox("OP14 – Consulta linea e orari", lines, 90);
    }

    // =========================
    // Utility
    // =========================

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
