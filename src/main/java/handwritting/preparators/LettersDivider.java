package handwritting.preparators;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import handwritting.Matrix;
import handwritting.utils.opencv.OpenCVWrapper;
import handwritting.utils.opencv.OpenCvColor;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LettersDivider {

    private static int WHITE_IDX = 0;
    private static int BLACK_COLOR = 0;
    private static int WHITE_COLOR = 255;
    private static int PIXEL_I_JOINED_AREA = 2;

    public static List<Mat> findLetters(Mat source) {
        List<Mat> letters = new ArrayList<>();
        Mat binarized = new Mat();
        Imgproc.threshold(source, binarized, 0.7 * WHITE_COLOR, WHITE_COLOR, Imgproc.THRESH_BINARY);
        Matrix binarizedSource = binarize(binarized, 0.7 * WHITE_COLOR);

        Matrix blackRegions = findBlackRegions(binarizedSource);

        joinDotsToI(blackRegions, PIXEL_I_JOINED_AREA);

        for (Integer value : blackRegions.uniqueValues()) {
            if (value != WHITE_IDX) {
                Matrix letterMaskMatrix = makeMaskOnValue(blackRegions, value);
                Mat letter = OpenCVWrapper.from2dArray1Channel(letterMaskMatrix.getData(),
                                                                   letterMaskMatrix.getRows(),
                                                                   letterMaskMatrix.getColumns(),
                                                                   CvType.CV_8UC(1));

                if(letter.size().height > 28 || letter.size().width > 28) {
                    return new ArrayList<Mat>();
                }

                OpenCVWrapper.resizeAndFillWithColor(letter, new Size(28, 28), OpenCvColor.WHITE.getScalar());
                letters.add(letter);
            }
        }

        return letters;
    }

    private static void joinDotsToI(Matrix matrix, int pixelJoinedArea) {
        for (Integer value : matrix.uniqueValues()) {
            if (value != WHITE_IDX && matrix.contains(value)) {
                int leftIdx = Math.max(0, matrix.firstLeftIdxOfValue(value)+1 - pixelJoinedArea);
                int rightIdx = Math.min(matrix.getColumns(), matrix.firstRightIdxOfValue(value)+1 + pixelJoinedArea);

                for (int y = 0; y < matrix.getRows(); y++) {
                    for (int x = leftIdx; x < rightIdx; x++) {
                        int v = matrix.getValue(y, x);
                        if (v != 0 && v != value) {
                            matrix.setValue(y, x, value);
                        }
                    }
                }
            }
        }
    }

    private static Matrix makeMaskOnValue(Matrix matrix, int value) {
        Matrix maskMatrix = matrix.binarizeOnValue(value);
        maskMatrix.invertValues();
        maskMatrix.multiply(WHITE_COLOR);
        maskMatrix.cropByValue(BLACK_COLOR);
        return maskMatrix;
    }

    private static Matrix findBlackRegions(Matrix matrix) {
        int[][] result = new int[matrix.getRows()][matrix.getColumns()];
        int[][] array = matrix.copyData();
        int val = 11;

        for (int x=0; x<matrix.getColumns(); x++) {
            for (int y=0; y<matrix.getRows(); y++) {
                if (array[y][x] == 1) {
                    count(array, y, x);
                    distribute(matrix.getData(), result, y, x, val);
                    val += 1;
                }
            }
        }

        Matrix resMatrix = new Matrix(result, matrix.getRows(), matrix.getColumns());
        resMatrix.setData(result);

        return resMatrix;
    }

    private static int count(int[][] array, int y, int x) {
        if (!valid(array, y, x)) return 0;
        if (array[y][x] == 0) return 0;

        array[y][x] = 0; // przestawienie, aby w rekurencji powrotnej pokazać, że jeszcze nie policzono tu sąsiadów
        return 1 +
                count(array, y-1, x) +
                count(array, y+1, x) +
                count(array, y, x-1) +
                count(array, y, x+1) +
                count(array, y-1, x+1) +
                count(array, y+1, x+1) +
                count(array, y-1, x-1) +
                count(array, y+1, x-1);
    }

    private static void distribute(
            int[][] inputArray, int[][] result, int y, int x, int value) {
        if (!valid(inputArray, y, x)) return; // jesteśmy poza obrazkiem
        if (inputArray[y][x] == 0) return; // wartość, której nie należy rozprzestrzeniać, bo to tło
        if (result[y][x] != 0) return; // już był update
        result[y][x] = value;
        distribute(inputArray, result, y-1, x, value);
        distribute(inputArray, result, y+1, x, value);
        distribute(inputArray, result, y, x-1, value);
        distribute(inputArray, result, y, x+1, value);
        distribute(inputArray, result, y-1, x+1, value);
        distribute(inputArray, result, y+1, x+1, value);
        distribute(inputArray, result, y-1, x-1, value);
        distribute(inputArray, result, y+1, x-1, value);
    }

    private static boolean valid(int array[][], int r, int c) {
        if (r < 0) return false;
        if (r >= array.length) return false;
        if (c < 0) return false;
        if (c >= array[r].length) return false;
        return true;
    }

    private static Matrix binarize(Mat source, double colorThreshold) {
        Matrix binarized = new Matrix(source.rows(), source.cols());
        for (int x = 0; x < source.cols(); x++) {
            for (int y = 0; y < source.rows(); y++) {
                if (source.get(y, x)[0] < colorThreshold) {
                    binarized.setValue(y, x, 1);
                } else {
                    binarized.setValue(y, x, 0);
                }
            }
        }
        return binarized;
    }
}
