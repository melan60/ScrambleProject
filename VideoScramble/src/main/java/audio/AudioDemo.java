package audio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.Core;



public class AudioDemo extends Application {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void start(Stage primaryStage) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Audio.fxml"));
            BorderPane rootElement = (BorderPane) loader.load();
            Scene scene = new Scene(rootElement, 1200, 800);

            primaryStage.setTitle("Capture audio");
            primaryStage.setScene(scene);
            primaryStage.show();

            TestController controller = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}


// faire une sinusoide pour le son x2
//AudioInputStream
//AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);

//byte[] audioData = new byte[bufferSize];
//line.read(audioData, 0, bufferSize);
// https://docs.oracle.com/javase%2F7%2Fdocs%2Fapi%2F%2F/javax/sound/sampled/AudioInputStream.html
// http://igm.univ-mlv.fr/~dr/XPOSE2005/JavaSound_arinie/exemples/recorder.html
// https://www.javacodex.com/Java-IO/AudioInputStream