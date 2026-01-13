package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseCliView {

    protected static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    // ==========================
    //  BOX ASCII/UNICODE
    // ==========================

    protected static void printBox(String title, List<String> lines, int innerWidth) {
        if (innerWidth < 10) innerWidth = 10;

        // Top
        System.out.println("╔" + "═".repeat(innerWidth + 2) + "╗");

        // Optional title row (centered)
        if (title != null && !title.isBlank()) {
            String t = " " + title.trim() + " ";
            if (t.length() > innerWidth + 2) t = t.substring(0, innerWidth + 2);
            System.out.println("║" + padCenter(t, innerWidth + 2) + "║");
            System.out.println("╠" + "═".repeat(innerWidth + 2) + "╣");
        }

        // Body lines (word-wrap, no truncation)
        for (String raw : lines) {
            String s = (raw == null) ? "" : raw;

            // Supporto anche righe con newline espliciti
            String[] logicalLines = s.split("\\R", -1);
            for (String logical : logicalLines) {
                for (String part : wrapLine(logical, innerWidth)) {
                    System.out.println("║ " + padRight(part, innerWidth) + " ║");
                }
            }
        }

        // Bottom
        System.out.println("╚" + "═".repeat(innerWidth + 2) + "╝");
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }

    /**
     * Spezza una singola riga in più righe che non superano `width`.
     * Word-wrap quando possibile; se una parola è più lunga di `width`, viene spezzata.
     */
    private static List<String> wrapLine(String line, int width) {
        List<String> out = new ArrayList<>();
        if (line == null) {
            out.add("");
            return out;
        }

        String remaining = line;
        // Manteniamo le stringhe vuote (es. per righe blank)
        if (remaining.isEmpty()) {
            out.add("");
            return out;
        }

        while (remaining.length() > width) {

            // Provo a spezzare sull'ultimo spazio entro il limite
            int lastSpace = remaining.lastIndexOf(' ', width);
            if (lastSpace > 0) {
                out.add(remaining.substring(0, lastSpace));
                remaining = remaining.substring(lastSpace + 1);
            } else {
                // Nessuno spazio utile: spezza duro
                out.add(remaining.substring(0, width));
                remaining = remaining.substring(width);
            }

            // Evito che la riga successiva inizi con spazi
            while (!remaining.isEmpty() && remaining.charAt(0) == ' ') {
                remaining = remaining.substring(1);
            }

            if (remaining.isEmpty()) {
                out.add("");
                return out;
            }
        }

        out.add(remaining);
        return out;
    }

    private static String padCenter(String s, int width) {
        if (s.length() >= width) return s;
        int left = (width - s.length()) / 2;
        int right = width - s.length() - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }

    // ==========================
    //  INPUT + MENU (boxed)
    // ==========================

    protected int chooseBox(String title, List<String> options) {
        // Calcolo larghezza interna box (in base a opzioni + numerazione)
        int max = 0;
        for (int i = 0; i < options.size(); i++) {
            String line = (i + 1) + ") " + options.get(i);
            max = Math.max(max, line.length());
        }
        int titleLen = (title == null) ? 0 : title.length();

        // Larghezza interna: deve contenere sia le opzioni sia il titolo.
        // Aumento il cap massimo per evitare troncamenti del titolo.
        int innerWidth = Math.max(30, Math.min(120, Math.max(max, titleLen)));

        while (true) {
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                lines.add((i + 1) + ") " + options.get(i));
            }
            lines.add(""); // riga vuota
            lines.add("Seleziona opzione (1-" + options.size() + "):");

            System.out.println();
            printBox(title, lines, innerWidth);

            try {
                System.out.print("> ");
                String input = reader.readLine();
                int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= options.size()) return choice;
                System.out.println("Scelta non valida.");
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero.");
            } catch (IOException e) {
                System.out.println("Errore lettura input.");
            }
        }
    }

    public static void pressEnterBox() {
        List<String> lines = List.of(
                "Premi INVIO per continuare..."
        );
        printBox("Info", lines, 50);
        try { reader.readLine(); } catch (IOException ignored) {}
    }

    // ==========================
    //  API pubblica "BOX"
    // ==========================

    public static void box(String title, List<String> lines) {
        int innerWidth = computeInnerWidth(title, lines);
        System.out.println();
        printBox(title, lines, innerWidth);
    }

    public static void box(String title, String message) {
        box(title, List.of(message));
    }

    private static int computeInnerWidth(String title, List<String> lines) {
        int max = 0;
        if (title != null) max = title.length();
        if (lines != null) {
            for (String s : lines) {
                if (s != null) {
                    // considera anche righe con \n
                    for (String part : s.split("\\R")) {
                        max = Math.max(max, part.length());
                    }
                }
            }
        }
        return Math.max(30, Math.min(120, max));
    }
}