package video;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Core;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

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
    // the FXML button
    @FXML
    private Button button;
    // the FXML image view
    @FXML
    private ImageView currentFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    //"http://192.168.1.10:8080/"
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    // when using apple OS with an associated iphone nearby, 0 will be iphone's cam
    private static int cameraId = 0;

    public int plus_grande_puissance_de_2(int height){
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
        System.out.println("Pow : " + puissance);
        System.out.println("Calcul pow : " + Math.pow(2,puissance));
        return (int) Math.pow(2,puissance);
    }

    public Image crypter(Image image_a_crypter){
        System.out.println("méthode crypter");

        //choisir r(codé sur 8bit) et s(codé sur 7bit)
        int r = 10; //décalage
        int s = 3; //le pas

        //Récupérer la taille de l'image
        int height = (int) image_a_crypter.getHeight();
        int width = (int) image_a_crypter.getWidth();
        System.out.println("Taille : " + height + ", " + width);

        System.out.println("méthode crypter1");

        PixelReader pixelReader = image_a_crypter.getPixelReader(); //Pour lire la couleur du pixel
        WritableImage new_image = new WritableImage(width, height);
        PixelWriter pixelWriter = new_image.getPixelWriter(); //pour écrire les pixels

        System.out.println("méthode crypter2");

        int iteration = plus_grande_puissance_de_2(height);
        System.out.println("itérataion : "+iteration);
        int currentHeight = height;
        int sumIteration = iteration;


//        while(iteration <= height-1){//boucle sur chaque itération
//
//            iteration = plus_grande_puissance_de_2(height-iteration);

        for(int idLigne=0; idLigne < height; idLigne++){ //boucle pour faire toutes les lignes de l'itération
//        for(int idLigne=height; idLigne > 0; idLigne--){ //boucle pour faire toutes les lignes de l'itération
//                System.out.println("testtt");
            if(idLigne==sumIteration){
                currentHeight = currentHeight - iteration;
                System.out.println("currentHeight : " + currentHeight);
                iteration = plus_grande_puissance_de_2(currentHeight);
                sumIteration += iteration;
                System.out.println("itérataion : "+iteration);
            }
            int newIdLigne = (r + (2 * s + 1) * idLigne) % currentHeight;
            for(int idColonne = 0; idColonne<width;idColonne++){
                pixelWriter.setColor(idColonne, newIdLigne, pixelReader.getColor(idColonne, idLigne));
//                pixelWriter.setPixels(idColonne, newIdLigne, pixelReader.getPixels(idColonne, idLigne, ));
            }
        }
//        }
        System.out.println("FIN");
        return new_image;
//        return null;
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

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run()
                    {
                        // effectively grab and process a single frame
                        // note : macbook & iphone 11 : 1080p
                        Mat frame = grabFrame();
                        // more complex image processing can be called from here
                        // convert and show the frame
                        Image imageToShow = mat2Image(frame);
                        System.out.println("test1");

                        //Méthode pour chiffrer
                        imageToShow = crypter(imageToShow);
                        System.out.println("test3");
                        updateImageView(currentFrame, imageToShow);
                        System.out.println("test4");

                        currentFrame.setFitWidth(800);
                        currentFrame.setPreserveRatio(true);

                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");
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
            this.button.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
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