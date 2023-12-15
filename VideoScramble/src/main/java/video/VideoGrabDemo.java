package video;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;


import javafx.stage.WindowEvent;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Core;
import video.VideoGrabDemoController;

import java.io.InputStream;

/**
 * Gère le lancement de l'application
 *
 * @author Messaline BEAUDRU
 * @author Mélanie BENOIT
 * @group S5-A1
 *
 */

public class VideoGrabDemo extends Application {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VideoGrabDemo.fxml"));
            BorderPane rootElement = (BorderPane) loader.load();

            Scene scene = new Scene(rootElement, 1200, 800);

            primaryStage.setTitle("Capture vidéo de la webcam avec OpenCV");
            primaryStage.setScene(scene);
            primaryStage.show();

            VideoGrabDemoController controller = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

