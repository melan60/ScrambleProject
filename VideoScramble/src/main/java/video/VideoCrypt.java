package video;

import org.opencv.core.Mat;

/**
 * Classe contenant les méthodes liées au cryptage et au décryptage
 *
 * @author Messaline BEAUDRU
 * @author Mélanie BENOIT
 * @group S5-A1
 *
 */

public class VideoCrypt {
    /**
     * Trouve la puissance de 2 la plus proche de la hauteur de l'image
     * @param height hauteur de l'image
     * @return la valeur de la puissance de 2 la plus proche de la hauteur de l'image
     */
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

    /**
     * Echange deux lignes d'une matrice
     * @param mat matrice
     * @param line1 une ligne à échanger avec la seconde
     * @param line2 une ligne à échanger avec la première
     * @return la matrice avec les lignes échangées
     */
    private static Mat swapLines(Mat mat, int line1, int line2) {
        Mat temp = mat.row(line1).clone();
        mat.row(line2).copyTo(mat.row(line1));
        temp.copyTo(mat.row(line2));
        return mat;
    }

    /**
     * Crypte l'image
     * @param matImage image à crypter
     * @param r décalage
     * @param s pas
     * @return l'image cryptée
     */
    public Mat crypter(Mat matImage, int r, int s){
        int height = matImage.height();

        int maxPowerOfTwo = findMaxPowerOfTwo(height);
        int currentHeight = height;
        int sumIteration = 0;
        int sumLinesUsed = maxPowerOfTwo;
        int newIdLigne;

        for(int idLigne=0; idLigne < height; idLigne++){
            if(idLigne==sumLinesUsed){
                currentHeight = currentHeight - maxPowerOfTwo;
                sumIteration += maxPowerOfTwo;
                maxPowerOfTwo = findMaxPowerOfTwo(currentHeight);
                sumLinesUsed += maxPowerOfTwo;
            }
            newIdLigne = ((r + (2 * s + 1) * idLigne) % maxPowerOfTwo) + sumIteration;
            matImage = swapLines(matImage, newIdLigne, idLigne);
        }
        return matImage;
    }

    /**
     * Décrypte l'image
     * @param matImageToDecrypt image à décrypter
     * @param r décalage
     * @param s pas
     * @return l'image décryptée
     */
    public Mat decrypter(Mat matImageToDecrypt, int r, int s){
        int height = matImageToDecrypt.height();

        int iteration = findMaxPowerOfTwo(height);
        int fixedIteration = iteration;
        int currentHeight = height;
        int sumIteration = 0;
        int idLigne;
        int previousIdLigne = -1;

        while(iteration != 0){
            idLigne = iteration-1;
            iteration--;
            previousIdLigne = ((r + (2 * s + 1) * idLigne) % fixedIteration) + sumIteration;
            matImageToDecrypt = swapLines(matImageToDecrypt, (idLigne+sumIteration), previousIdLigne);
            if(iteration == 0){
                currentHeight = currentHeight - fixedIteration;
                sumIteration += fixedIteration;
                iteration = findMaxPowerOfTwo(currentHeight);
                fixedIteration = iteration;
                if(iteration == 1 || iteration == 0) {
                    break;
                }
            }
        }
        return matImageToDecrypt;
    }
}
