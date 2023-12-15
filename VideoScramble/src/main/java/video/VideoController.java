package video;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

/**
 * Le controleur de notre application qui s'occupe de l'entièreté
 * des actions.
 *
 * @author Messaline BEAUDRU
 * @author Mélanie BENOIT
 * @group S5-A1
 *
 */
public class VideoController
{
    @FXML
    private VBox vboxRadioButton;
    @FXML
    private TextField valueR;
    @FXML
    private TextField valueS;
    @FXML
    private ToggleGroup group;
    @FXML
    private Button buttonAction;
    @FXML
    private ImageView currentFrame;
    @FXML
    private ImageView currentFrame2;
    @FXML
    private ImageView currentFrame3;

    private VideoCrypt videoCrypt = new VideoCrypt();
    private VideoVue videoVue = new VideoVue();
    private VideoWriter videoWriter;
    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;


    /**
     * Méthode appelée au lancement de l'application
     * @param event l'évènement
     */
    @FXML
    protected void launchApp(ActionEvent event){
        if(this.buttonAction.getText().equals("Parcourir") || this.buttonAction.getText().equals("Stop parcourir")){
            if(!this.cameraActive) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Media");
                File selectedFile = fileChooser.showOpenDialog(null);

                if (selectedFile != null) {
                    String url = selectedFile.getAbsolutePath();
                    this.capture.open(url);
                }
            }
            else {
                this.cameraActive = false;
                this.buttonAction.setText("Parcourir");
                videoVue.stopAcquisition(this.timer, this.capture, this.videoWriter);
                return;
            }
        }
        else {
            if (!this.cameraActive) {
                this.capture.open(cameraId);
            }
            else {
                this.cameraActive = false;
                this.buttonAction.setText("Start Camera");
                // stop the timer and save the video
                videoVue.stopAcquisition(this.timer, this.capture, this.videoWriter);
                return;
            }
        }

        if(this.capture.isOpened()){
            int[] values = videoVue.checkValues(this.valueR,this.valueS);
            if(values == null){
                // TODO message d'erreur
                this.cameraActive = false;
                videoVue.stopAcquisition(this.timer, this.capture, this.videoWriter);
                System.out.println("error r & s values");
                return;
            }
            this.cameraActive = true;
            Size frameSize = new Size(this.capture.get(Videoio.CAP_PROP_FRAME_WIDTH), this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
            this.videoWriter = new VideoWriter("output.mp4", VideoWriter.fourcc('X', '2', '6', '4'), 30, frameSize);

            Runnable frameGrabber = new Runnable() {
                @Override
                public void run() {
                    Mat frame = new Mat();
                    if(!buttonAction.getText().equals("Parcourir")) {
                        frame = videoVue.grabFrame(capture);
//                        frame = Imgcodecs.imread("/home/mbenoit/Documents/S5/ProgMedia/ScrambleProject/VideoScramble/src/main/resources/video/yoda.jpg");
                    }
                    else {
                        capture.read(frame);
                    }
                    Mat cryptedFrame;

                    Image imageToShow = videoVue.mat2Image(frame);
                    videoVue.updateImageView(currentFrame, imageToShow);

                    cryptedFrame = videoCrypt.crypter(frame, values[0], values[1]);
                    imageToShow = videoVue.mat2Image(cryptedFrame);
                    videoVue.updateImageView(currentFrame2, imageToShow);

                    if (videoWriter.isOpened()) {
                        videoWriter.write(cryptedFrame);
                    }

//                    System.out.println(group.getSelectedToggle());
                    frame = videoCrypt.decrypter(cryptedFrame, values[0], values[1]);
                    imageToShow = videoVue.mat2Image(frame);
                    videoVue.updateImageView(currentFrame3, imageToShow);

                    if(frame.empty()){
                        videoVue.stopAcquisition(timer, capture, videoWriter);
                    }
                }
            };
            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            if(!this.buttonAction.getText().equals("Parcourir")){
                this.buttonAction.setText("Stop webcam");
            }
            else this.buttonAction.setText("Stop parcourir");
        }
        else {
            System.err.println("Impossible d'ouvrir la connexion...");
        }
    }

    /**
     * Méthode appelée lorsqu'on clique sur le bouton "Démarrer la Webcam"
     */
    @FXML
    void toggleWebcam() {
        buttonAction.setText("Démarrer la Webcam");
        vboxRadioButton.setVisible(false);
    }

    /**
     * Méthode appelée lorsqu'on clique sur le bouton "Parcourir"
     */
    @FXML
    void toggleFile() {
        buttonAction.setText("Parcourir");
        vboxRadioButton.setVisible(true);
    }
}