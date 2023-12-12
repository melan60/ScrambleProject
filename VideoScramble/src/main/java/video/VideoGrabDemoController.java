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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;

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
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    // when using apple OS with an associated iphone nearby, 0 will be iphone's cam
    private static int cameraId = 0;

    public int findMaxPowerOfTwo(int height){
        int val = 1;
        int puissance = 0;
        if(height == 0)
            return 0;
        while(val <= height){
            val = val*2;
            puissance++;
        }
        if(val > height){
            puissance = puissance-1;
        }
        return (int) Math.pow(2,puissance);
    }

    public Mat crypter(Mat matImage, Image toCrypt){
        //choisir r(codé sur 8bit) et s(codé sur 7bit)
        int r = 48; //décalage
        int s = 200; //le pas

        //Récupérer la taille de l'image
        int height = (int) toCrypt.getHeight();

//        matImage.row(4) // TODO
//        decrypt = matImage.row(i).copyTo(truc avec le calcul);

//        PixelReader pixelReader = toCrypt.getPixelReader(); //Pour lire la couleur du pixel
//        WritableImage cryptedImage = new WritableImage(width, height);
//        PixelWriter pixelWriter = cryptedImage.getPixelWriter(); //pour écrire les pixels

        int iteration = findMaxPowerOfTwo(height);
        int currentHeight = height;
        int sumIteration = 0;
        int sum = iteration;

        for(int idLigne=0; idLigne < height; idLigne++){
            if(idLigne==sum){
                currentHeight = currentHeight - iteration;
//                System.out.println("currentHeight : " + currentHeight);
                sumIteration += iteration;
                iteration = findMaxPowerOfTwo(currentHeight);
                sum += iteration;
//                System.out.println("itérataion : "+iteration);
            }
            int newIdLigne = (r + (2 * s + 1) * idLigne) % (iteration);
            int test = newIdLigne + sumIteration;
//            System.out.println("newLigne : " + test);
            matImage = swapLines(matImage, idLigne, test);
        }
        return matImage;
    }

    // Fonction pour échanger deux lignes dans une image
    private static Mat swapLines(Mat image, int ligne1, int ligne2) {
        // Extraire les lignes spécifiées
        Mat ligne1Mat = image.row(ligne1);
        Mat ligne2Mat = image.row(ligne2);

        // Copier le contenu de la ligne 1 vers la ligne 2 et vice versa
        ligne1Mat.copyTo(image.row(ligne2));
//        ligne2Mat.copyTo(image.row(ligne1));
        return image;
    }

    /**
     * The action triggered by pushing the button on the GUI
     *
     * @param event
     *            the push button event
     */
    @FXML
    protected void startCamera(ActionEvent event)
    {

        if (!this.cameraActive)
        {
            // start the video capture
            this.capture.open(cameraId);
            // is the video stream available?
            if (this.capture.isOpened())
            {
                this.cameraActive = true;

                int[] values = checkValues();
                if(values == null){
                    this.cameraActive = false;
                    this.stopAcquisition();
                    System.out.println("error r & s values");
                    return;
                }

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run()
                    {
                        // effectively grab and process a single frame
                        // note : macbook & iphone 11 : 1080p


                        Mat frame = grabFrame();
//                        Mat frame = Imgcodecs.imread("/home/mbenoit/Documents/S5/ProgMedia/ScrambleProject/VideoScramble/src/main/resources/video/yoda.jpg");

                        // more complex image proce
                        // ssing can be called from here
                        // convert and show the frame
                        Image imageToShow = mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);

                        //Méthode pour chiffrer
                        frame = crypter(frame, imageToShow);
                        imageToShow = mat2Image(frame);
                        updateImageView(currentFrame2, imageToShow);
                        updateImageView(currentFrame3, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.buttonWebcam.setText("Stop Camera");
            }
            else
            {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        }
        else
        {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.buttonWebcam.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
    }

    public int[] checkValues() {
        int[] values = new int[2];
        try {
            values[0] = Integer.parseInt(this.valueR.getText());
            values[1] = Integer.parseInt(this.valueS.getText());
        } catch (NumberFormatException e) {
            System.out.println(e);
            return null;
        }
        if(values[0] < 0 || values[1] < 0 || values[0] > 255 || values[1] > 127)
            return null;
        return values;
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Mat} to show
     */
    private Mat grabFrame()
    {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened())
        {
            try
            {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty())
                {
                    // basic single frame processing can be performed here
                    // TODO ?
                }

            }
            catch (Exception e)
            {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
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

        if(selectedFile != null){
            String url = selectedFile.toURI().toString();

//            media = new Media(url);
//            mediaPlayer = new MediaPlayer(media);
//
//            mediaView.setMediaPlayer(mediaPlayer);
//
//            mediaPlayer.currentTimeProperty().addListener(((observableValue, oldValue, newValue) -> {
//                slider.setValue(newValue.toSeconds());
//                lblDuration.setText("Duration: " + (int)slider.getValue() + " / " + (int)media.getDuration().toSeconds());
//            }));
//
//            mediaPlayer.setOnReady(() ->{
//                Duration totalDuration = media.getDuration();
//                slider.setMax(totalDuration.toSeconds());
//                lblDuration.setText("Duration: 00 / " + (int)media.getDuration().toSeconds());
//            });
//
//            Scene scene = mediaView.getScene();
//            mediaView.fitWidthProperty().bind(scene.widthProperty());
//            mediaView.fitHeightProperty().bind(scene.heightProperty());

            //mediaPlayer.setAutoPlay(true);

        }

    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition()
    {
        if (this.timer!=null && !this.timer.isShutdown())
        {
            try
            {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened())
        {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view
     *            the {@link ImageView} to update
     * @param image
     *            the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image)
    {
        onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed()
    {
        this.stopAcquisition();
    }

    private Image matToJavaFXImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new java.io.ByteArrayInputStream(buffer.toArray()));
    }


    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame
     *            the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    public static Image mat2Image(Mat frame)
    {
        try
        {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        }
        catch (Exception e)
        {
            System.err.println("Cannot convert the Mat obejct: " + e);
            return null;
        }
    }

    /**
     * @param original
     *            the {@link Mat} object in BGR or grayscale
     * @return the corresponding {@link BufferedImage}
     */
    private static BufferedImage matToBufferedImage(Mat original)
    {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1)
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        }
        else
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     *
     * @param property
     *            a {@link ObjectProperty}
     * @param value
     *            the value to set for the given {@link ObjectProperty}
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value)
    {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

}