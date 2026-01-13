package view;

import java.util.List;
import java.time.LocalTime;

/**
 * Vista del menu iniziale (pubblico / login / esci).
 */
public class MainMenuView extends BaseCliView {

    public enum Choice {
        PUBBLICO,
        LOGIN,
        ESCI
    }

    /**
     * Mostra il menu iniziale e restituisce la scelta.
     */
    public Choice show() {
        String saluto = LocalTime.now().isBefore(LocalTime.of(18, 0)) ? "Buongiorno" : "Buonasera";
        String titolo = saluto + "! Benvenuto nel Sistema Informativo di Trasporto Pubblico";

        int c = chooseBox(
                titolo,
                List.of(
                        "Consulta orari e linee",
                        "Login",
                        "Esci"
                )
        );

        if (c == 3) {
            System.out.println("Arrivederci!");
        }

        return switch (c) {
            case 1 -> Choice.PUBBLICO;
            case 2 -> Choice.LOGIN;
            default -> Choice.ESCI;
        };
    }
}