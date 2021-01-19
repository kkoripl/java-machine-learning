package djl.handwritting.service;

import djl.config.lettersDivider.LettersBinarizedProperties;
import djl.config.lettersDivider.LettersColorProperties;
import djl.config.lettersDivider.LettersDividerProperties;
import djl.utils.opencv.OpenCVWrapper;
import djl.utils.opencv.OpenCvColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import djl.handwritting.dto.*;

@Builder
public class LettersDivider {

    private final OpenCVWrapper openCvWrapper;
    @Builder.Default
    private final int joinedIAreaPx = 2;
    private final String templateFilePath;
    @Builder.Default
    private final int binarizedWhite = 0;
    @Builder.Default
    private final int binarizedBlack = 1;
    @Builder.Default
    private final double colorThreshold = 0.7;
    @Builder.Default
    private final double templateSimilarityThreshold = 0.65;
    @Builder.Default
    private final int white1dColor = 255;
    @Builder.Default
    private final int black1dColor = 0;
    private final int imgHeight;
    private final int imgWidth;

    public List<Mat> divideLetters(byte[] imgBytes) {
        return divideLetters(openCvWrapper.bytes2Mat(imgBytes));
    }

    public List<Mat> divideLetters(Mat img) {
        List<Mat> letters = new ArrayList<>();
        Mat binarizedWhiteBlack = new Mat();

        img = cleanImage(img);
        Imgproc.threshold(img, binarizedWhiteBlack, colorThreshold * white1dColor, white1dColor, Imgproc.THRESH_BINARY);
        Matrix binarizedSource = binarize(binarizedWhiteBlack, colorThreshold * white1dColor);
        Matrix blackRegions = findBlackRegions(binarizedSource);

        joinDotsToI(blackRegions, joinedIAreaPx);

        for (Integer value : blackRegions.uniqueValues()) {
            if (value != binarizedWhite) {
                Matrix letterMaskMatrix = makeMaskOnValue(blackRegions, value);
                Mat letter = openCvWrapper.from2dArray1Channel(letterMaskMatrix.getData(),
                                                                   letterMaskMatrix.getRows(),
                                                                   letterMaskMatrix.getColumns(),
                                                                   CvType.CV_8UC(1));

                if(letter.size().height > imgHeight || letter.size().width > imgWidth) {
                    return new ArrayList<Mat>();
                }

                openCvWrapper.resizeAndFillWithColor(letter, new Size(imgWidth, imgHeight), OpenCvColor.WHITE.getScalar());
                letters.add(letter);
            }
        }

        return letters;
    }

    private Mat cleanImage(Mat img) {
        Mat template = OpenCVWrapper.loadImage(templateFilePath);
        return openCvWrapper.cleanImage(img, template, templateSimilarityThreshold);
    }

    private void joinDotsToI(Matrix matrix, int pixelJoinedArea) {
        for (Integer value : matrix.uniqueValues()) {
            if (value != binarizedWhite && matrix.contains(value)) {
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

    private Matrix makeMaskOnValue(Matrix matrix, int value) {
        Matrix maskMatrix = matrix.binarizeOnValue(value);
        maskMatrix.invertValues();
        maskMatrix.multiply(white1dColor);
        maskMatrix.cropByValue(black1dColor);
        return maskMatrix;
    }

    private Matrix findBlackRegions(Matrix matrix) {
        int[][] result = new int[matrix.getRows()][matrix.getColumns()];
        int val = 11;

        for (int x=0; x<matrix.getColumns(); x++) {
            for (int y=0; y<matrix.getRows(); y++) {
                if (matrix.getValue(y, x) == 1) {
                    distribute(matrix.getData(), result, y, x, val);
                    val += 1;
                }
            }
        }

        return new Matrix(result, matrix.getRows(), matrix.getColumns());
    }

    private void distribute(
            int[][] inputArray, int[][] result, int y, int x, int value) {
        if (!valid(inputArray, y, x)) {
            return; // jesteśmy poza obrazkiem
        }
        if (inputArray[y][x] == 0) {
            return; // wartość, której nie należy rozprzestrzeniać, bo to tło
        }
        if (result[y][x] != 0) {
            return; // już był update
        }
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

    private boolean valid(int[][] array, int r, int c) {
        if (r < 0) return false;
        if (r >= array.length) return false;
        if (c < 0) return false;
        return c < array[r].length;
    }

    private Matrix binarize(Mat source, double colorThreshold) {
        Matrix binarized = new Matrix(source.rows(), source.cols());
        for (int x = 0; x < source.cols(); x++) {
            for (int y = 0; y < source.rows(); y++) {
                if (source.get(y, x)[0] < colorThreshold) {
                    binarized.setValue(y, x, binarizedBlack);
                } else {
                    binarized.setValue(y, x, binarizedWhite);
                }
            }
        }
        return binarized;
    }
}
