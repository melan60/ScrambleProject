package video;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

/**
 * The controller for our application, where the application logic is
 * implemented. It handles the button for starting/stopping the camera and the
 * acquired video stream.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a> (minor fixes)
 * @version 2.0 (2016-09-17)
 * @since 1.0 (2013-10-20)
 *
 */
public class VideoGrabDemoController
{
    @FXML
    private VBox valueFields;

    @FXML
    private TextField valueR;

    @FXML
    private TextField valueS;
    @FXML
    private Button buttonFile;

    // the FXML button
    @FXML
    private Button buttonWebcam;

    @FXML
    private ToggleButton toggleButtonWebcam;

    @FXML
    private ToggleButton toggleButtonFile;
    // the FXML image view
    @FXML
    private ImageView currentFrame;

    @FXML
    private ImageView currentFrame2;

    @FXML
    private ImageView currentFrame3;
    private VideoCrypt videoCrypt = new VideoCrypt();
    private VideoVue videoVue = new VideoVue();
    private VideoWriter videoWriter;
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    // when using apple OS with an associated iphone nearby, 0 will be iphone's cam
    private static int cameraId = 0;




    /**
     * The action triggered by pushing the button on the GUI
     *
     * @param event
     *            the push button event
     */
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(cameraId);
            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                Size frameSize = new Size(this.capture.get(Videoio.CAP_PROP_FRAME_WIDTH), this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT));

                this.videoWriter = new VideoWriter("output.mp4", VideoWriter.fourcc('X', '2', '6', '4'), 30, frameSize);
                System.out.println(videoWriter.isOpened());

                int[] values = videoVue.checkValues(this.valueR,this.valueS);
                if(values == null){
                    this.cameraActive = false;
                    videoVue.stopAcquisition(this.timer, this.capture, this.videoWriter);
                    System.out.println("error r & s values");
                    return;
                }
                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
//                        Mat frame = videoVue.grabFrame(this.capture);
                        Mat frame = Imgcodecs.imread("/home/mbenoit/Documents/S5/ProgMedia/ScrambleProject/VideoScramble/src/main/resources/video/yoda.jpg");
                        Mat cryptedFrame;
//                        Mat frame = Imgcodecs.imread("/home/mbeaudru/ecole/S5/Perrot/Projet/yoda1.png");

                        Image imageToShow = videoVue.mat2Image(frame);
                        videoVue.updateImageView(currentFrame, imageToShow);

                        //Méthode pour chiffrer
                        cryptedFrame = videoCrypt.crypter(frame);
                        imageToShow = videoVue.mat2Image(cryptedFrame);
                        videoVue.updateImageView(currentFrame2, imageToShow);

                        frame = videoCrypt.decrypter(cryptedFrame);
                        imageToShow = videoVue.mat2Image(frame);
                        videoVue.updateImageView(currentFrame3, imageToShow);

                        if (videoWriter.isOpened()) {
                            videoWriter.write(frame);
                            System.out.println("osopja");
                        }
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
                // update the button content
                this.buttonWebcam.setText("Stop Camera");
            }
            else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        }
        else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.buttonWebcam.setText("Start Camera");

            // stop the timer
            videoVue.stopAcquisition(this.timer, this.capture, this.videoWriter);
        }
    }

    @FXML
    void toggleWebcam() {
        toggleButton(buttonWebcam, toggleButtonWebcam, toggleButtonFile, buttonFile);
    }

    @FXML
    void toggleFile() {
        toggleButton(buttonFile, toggleButtonFile, toggleButtonWebcam, buttonWebcam);
    }

    private void toggleButton(Button buttonFile, ToggleButton toggleButtonFile, ToggleButton toggleButtonWebcam, Button buttonWebcam) {
        boolean isButtonVisible = buttonFile.isVisible();
        valueFields.setVisible(!isButtonVisible);
        valueFields.setManaged(!isButtonVisible);
        toggleButtonFile.setSelected(!isButtonVisible);
        toggleButtonWebcam.setSelected(false);
        buttonFile.setVisible(!isButtonVisible);
        buttonWebcam.setVisible(false);
    }

    @FXML
    void browseFile(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Media");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String url = selectedFile.getAbsolutePath();
            System.out.println(url);
            // start the video capture
            this.capture.open("resources/video.mp4");
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                Size frameSize = new Size(this.capture.get(Videoio.CAP_PROP_FRAME_WIDTH), this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT));

                this.videoWriter = new VideoWriter("output.mp4", VideoWriter.fourcc('X', '2', '6', '4'), 30, frameSize);
                System.out.println(videoWriter.isOpened());

                int[] values = videoVue.checkValues(this.valueR, this.valueS);
                if (values == null) {
                    this.cameraActive = false;
                    videoVue.stopAcquisition(this.timer, this.capture, this.videoWriter);
                    System.out.println("error r & s values");
                    return;
                }
                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
//                        Mat frame = videoVue.grabFrame(this.capture);
//                        Mat frame = Imgcodecs.imread("/home/mbenoit/Documents/S5/ProgMedia/ScrambleProject/VideoScramble/src/main/resources/video/yoda.jpg");
                        Mat cryptedFrame;
                        Mat frame = Imgcodecs.imread("/resources/video/yoda.jpg");

                        Image imageToShow = videoVue.mat2Image(frame);
                        videoVue.updateImageView(currentFrame, imageToShow);

                        //Méthode pour chiffrer
                        cryptedFrame = videoCrypt.crypter(frame);
                        imageToShow = videoVue.mat2Image(cryptedFrame);
                        videoVue.updateImageView(currentFrame2, imageToShow);

                        frame = videoCrypt.decrypter(cryptedFrame);
                        imageToShow = videoVue.mat2Image(frame);
                        videoVue.updateImageView(currentFrame3, imageToShow);

                        if (videoWriter.isOpened()) {
                            videoWriter.write(frame);
                            System.out.println("osopja");
                        }
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
                // update the button content
                this.buttonWebcam.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }

        }
    }
}