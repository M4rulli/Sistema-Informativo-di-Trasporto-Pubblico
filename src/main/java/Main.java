
/**
 * Entry point dell'applicazione.
 */
import controller.MainController;
import view.MainMenuView;

public final class Main {

    public static void main(String[] args) {
        MainMenuView mainMenuView = new MainMenuView();
        MainController mainController = new MainController(mainMenuView);

        mainController.run();
    }
}