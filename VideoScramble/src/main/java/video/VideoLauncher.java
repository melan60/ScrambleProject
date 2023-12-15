package video;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;


import org.opencv.core.Core;

/**
 * Gère le lancement de l'application
 *
 * @author Messaline BEAUDRU
 * @author Mélanie BENOIT
 * @group S5-A1
 *
 */

public class VideoLauncher extends Application {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    /**
     * Lance l'application
     * @param primaryStage la fenêtre principale
     * @throws Exception si l'application ne peut pas être lancée
     */
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VideoGrabDemo.fxml"));
            BorderPane rootElement = (BorderPane) loader.load();

            Scene scene = new Scene(rootElement, 1200, 800);

            primaryStage.setTitle("Capture vidéo de la webcam avec OpenCV");
            primaryStage.setScene(scene);
            primaryStage.show();

            VideoController controller = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lance l'application
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}

