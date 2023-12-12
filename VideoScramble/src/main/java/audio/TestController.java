package audio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.File;

public class TestController {
    @FXML
    Button buttonFile;

    @FXML
    Button record;
    private Media media;
    private MediaPlayer mediaPlayer;
    @FXML
    private MediaView mediaView;
    @FXML
    private Slider slider;
    @FXML
    private javafx.scene.control.Label lblDuration;


    @FXML
    void selectMedia(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Media");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String url = selectedFile.toURI().toString();

            media = new Media(url);
            mediaPlayer = new MediaPlayer(media);

            mediaView.setMediaPlayer(mediaPlayer);

            mediaPlayer.currentTimeProperty().addListener(((observableValue, oldValue, newValue) -> {
                slider.setValue(newValue.toSeconds());
                lblDuration.setText("Duration: " + (int) slider.getValue() + " / " + (int) media.getDuration().toSeconds());
            }));

            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = media.getDuration();
                slider.setMax(totalDuration.toSeconds());
                lblDuration.setText("Duration: 00 / " + (int) media.getDuration().toSeconds());
            });

            Scene scene = mediaView.getScene();
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());

            //mediaPlayer.setAutoPlay(true);

        }
    }
}
