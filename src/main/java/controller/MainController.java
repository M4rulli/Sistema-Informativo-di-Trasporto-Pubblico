package controller;

import model.LoginResult;
import model.Role;
import view.ConducenteView;
import view.GestoreView;
import view.LoginView;
import view.MainMenuView;
import view.PublicView;

public class MainController {

    private final MainMenuView mainMenuView;
    private final LoginController loginController;
    private final PublicController publicController;
    private final ConducenteController conducenteController;
    private final GestoreController gestoreController;

    public MainController(MainMenuView mainMenuView) {
        this.mainMenuView = mainMenuView;

        this.loginController = new LoginController(new LoginView());
        this.publicController = new PublicController(new PublicView());
        this.conducenteController = new ConducenteController(new ConducenteView());
        this.gestoreController = new GestoreController(new GestoreView());
    }

    public void run() {
        boolean running = true;

        while (running) {
            MainMenuView.Choice choice = mainMenuView.show();

            switch (choice) {

                case PUBBLICO -> publicController.run();

                case LOGIN -> {
                    LoginResult res = loginController.loginOnce();
                    if (res == null) {
                        // annullato (/back) o fallito
                        continue;
                    }

                    Role role = res.getRuolo();

                    if (role == Role.CONDUCENTE) {
                        conducenteController.run();

                    } else if (role == Role.GESTORE) {
                        gestoreController.run();
                    }
                }

                case ESCI -> running = false;
            }
        }
    }
}