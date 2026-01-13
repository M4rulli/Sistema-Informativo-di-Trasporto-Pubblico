package controller;

import model.LoginResult;
import model.Role;
import view.BaseCliView;
import view.LoginView;
import view.MainMenuView;
import view.PublicView;

/**
 * Controller principale: loop dell'app e routing.
 */
public class MainController {

    private final MainMenuView mainMenuView;
    private final LoginController loginController;
    private final PublicController publicController;

    // in futuro: UtenteController, ConducenteController, GestoreController
    // (che a loro volta usano UtenteView/ConducenteView/GestoreView)

    public MainController(MainMenuView mainMenuView) {
        this.mainMenuView = mainMenuView;
        LoginView loginView = new LoginView();
        this.loginController = new LoginController(loginView);
        this.publicController = new PublicController(new PublicView());
    }

    public void run() {
        boolean running = true;

        while (running) {
            MainMenuView.Choice choice = mainMenuView.show();

            switch (choice) {

                case PUBBLICO -> {
                    // Consultazione pubblica (DB user: utente) -> OP01 / OP14
                    publicController.run();
                }

                case LOGIN -> {
                    // Login staff (DB user: login) -> poi ruolo effettivo: CONDUCENTE o GESTORE
                    LoginResult res = loginController.loginOnce();
                    if (res == null) {
                        // annullato (/back) o fallito: torno al menu principale
                        continue;
                    }

                    Role role = res.getRuolo();

                    if (role == Role.CONDUCENTE) {
                        System.out.println("Menu CONDUCENTE da implementare (OP02/OP12/OP13/OP01/OP14)");
                        BaseCliView.pressEnterBox();
                        // TODO: conducenteController.run(res);

                    } else if (role == Role.GESTORE) {
                        System.out.println("Menu GESTORE da implementare (OP03..OP11)");
                        BaseCliView.pressEnterBox();
                        // TODO: gestoreController.run(res);

                    }
                }

                case ESCI -> {
                    running = false;
                }
            }
        }
    }
}