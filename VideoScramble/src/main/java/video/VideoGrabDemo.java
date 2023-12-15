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

        //VideoCapture capture = new VideoCapture(0); // 0 indique la première webcam
        //Mat frame = new Mat();

        try {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VideoGrabDemo.fxml"));
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("VideoFX.fxml"));
            // store the root element so that the controllers can use it
            BorderPane rootElement = (BorderPane) loader.load();


            Scene scene = new Scene(rootElement, 1200, 800);

            primaryStage.setTitle("Capture vidéo de la webcam avec OpenCV");
            primaryStage.setScene(scene);
            primaryStage.show();

            // set the proper behavior on closing the application
            VideoGrabDemoController controller = loader.getController();
//            TestController controller = loader.getController();
//            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
//                public void handle(WindowEvent we) {
//                    controller.setClosed();
//                }
//            }));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}

