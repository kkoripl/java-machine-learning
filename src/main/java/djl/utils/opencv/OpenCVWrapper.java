package djl.utils.opencv;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static djl.utils.opencv.OpenCvColor.WHITE;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;

public class OpenCVWrapper {

    @PostConstruct
    void loadOpenCv() {
        loadLibrary();
    }

    public void loadLibrary() {
        OpenCV.loadShared();
    }

    public void resizeImagesAndFillWithColor(String datasetPath, Size dimension, Scalar color) throws IOException {
        System.out.println("Resizing");
        Stream<Path> walk = Files.walk(Paths.get(datasetPath));
        walk.filter(Files::isRegularFile)
                .forEach(path -> {
                    Mat source = loadImage(path.toString());
                    resizeAndFillWithColor(source, dimension, color);
                    saveImage(source, path.toString());
                });
    }

    public void resizeAndFillWithColor(Mat source, Size dimension, Scalar color) {
        int top = (int)(dimension.height - source.size().height)/2;
        int bottom = (int)(dimension.height - source.size().height)/2;
        int left = (int)(dimension.width - source.size().width)/2;
        int right = (int)(dimension.width - source.size().width)/2;

        if (top + source.size().height + bottom != dimension.height) {
            top += 1;
        }

        if (left + source.size().width + right != dimension.width) {
            left += 1;
        }

        Core.copyMakeBorder(source, source, top, bottom, left, right, Core.BORDER_CONSTANT, color);
    }

    public Size findBiggestDimension(String datasetPath) throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(datasetPath));
        double maxHeight = 0;
        double maxWidth = 0;
        for (Path imgPath : walk.filter(Files::isRegularFile).collect(Collectors.toList())) {
            Size size = loadImage(imgPath.toString()).size();
            if (maxHeight < size.height) maxHeight = size.height;
            if (maxWidth < size.width) maxWidth = size.width;
        }

        return new Size(maxWidth, maxHeight);
    }

    public void cleanImages(String datasetPath, String notWantedPartPath, double similarityThreshold) throws IOException {
        System.out.println("Cleaning");
        Stream<Path> walk = Files.walk(Paths.get(datasetPath));
        walk.filter(Files::isRegularFile)
                .forEach(path -> {
                    Mat template = loadImage(notWantedPartPath);
                    Mat img = loadImage(path.toString());
                    img = cleanImage(img, template, similarityThreshold);
                    saveImage(img, path.toString());
                });
    }

    public Mat cleanImage(Mat img, Mat template, double similarityThreshold) {
        double whiteColorThreshold = 230;

        findAndRemoveTemplate(img, template, Imgproc.TM_CCOEFF_NORMED, similarityThreshold);
        img = convertToColorScale(img, Imgproc.COLOR_RGB2GRAY);
        return removeNoise(img, whiteColorThreshold);
    }

    private void findAndRemoveTemplate(Mat source, Mat template, int method, double threshold) {
        Mat findTemplateResult = findTemplateInImg(source, template, method);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(findTemplateResult);
        if (mmr.maxVal >= threshold) {
            removeTemplateFromImg(source, template, mmr.maxLoc, WHITE.getScalar());
        }
    }

    private Mat removeNoise(Mat source, double colorThreshold) {
        removeNoiseFromTheLeft(source, colorThreshold);
        removeNoiseFromTheRight(source, colorThreshold);
        removeNoiseFromTheTop(source, colorThreshold);
        removeNoiseFromTheBottom(source, colorThreshold);
        return source;
    }


    private void removeNoiseFromTheTop(Mat source, double colorThreshold) {
        for (int y = 2; y < source.rows()/2; y++) {
            if (wholeRowIsWhite(source, y, colorThreshold)) {
                MatOfPoint rect = rectContourFromPoints(0, 0, source.cols(), y);
                fillPoly(source, Collections.singletonList(rect), WHITE.getScalar());
                break;
            }
        }
    }

    private void removeNoiseFromTheBottom(Mat source, double colorThreshold) {
        for (int y = source.rows()-1-2; y > source.rows()/2; y--) {
            if (wholeRowIsWhite(source, y, colorThreshold)) {
                MatOfPoint rect = rectContourFromPoints(0, y, source.cols(), source.rows() - y);
                fillPoly(source, Collections.singletonList(rect), WHITE.getScalar());
                break;
            }
        }
    }

    private void removeNoiseFromTheLeft(Mat source, double colorThreshold) {
        for (int x = 2; x < source.cols()/2; x++) {
            if (wholeColumnIsWhite(source, x, colorThreshold)) {
                MatOfPoint rect = rectContourFromPoints(0, 0, x, source.rows());
                fillPoly(source, Collections.singletonList(rect), WHITE.getScalar());
                break;
            }
        }
    }

    private void removeNoiseFromTheRight(Mat source, double colorThreshold) {
        for (int x = source.cols()-1-2; x > source.cols()/2; x--) {
            if (wholeColumnIsWhite(source, x, colorThreshold)) {
                MatOfPoint rect = rectContourFromPoints(x, 0, source.cols() - x, source.rows());
                fillPoly(source, Collections.singletonList(rect), WHITE.getScalar());
                break;
            }
        }
    }

    private boolean wholeRowIsWhite(Mat source, int row, double colorThreshold) {
        return IntStream.range(0, source.cols())
                .allMatch(col -> Arrays.stream(source.get(row, col))
                        .allMatch(v -> v > colorThreshold));
    }

    private boolean wholeColumnIsWhite(Mat source, int col, double colorThreshold) {
        return IntStream.range(0, source.rows())
                .allMatch(row -> Arrays.stream(source.get(row, col))
                        .allMatch(v -> v > colorThreshold));
    }

    private Mat cropToText(Mat source, double colorThreshold) {

        double up = source.size(0);
        double down = 0;
        double left = source.size(1);
        double right = 0;
        Size kernelSize = source.size();

        // Blur potrzebuje kernela o wielkościach nieparzystych
        if (kernelSize.height % 2 == 0) kernelSize.height += 1;
        if (kernelSize.width % 2 == 0) kernelSize.width += 1;

        Mat imgCopy = new Mat();
        Imgproc.GaussianBlur(source, imgCopy, kernelSize, 0.5);

        // poszukiwania największego prostokąta, w którym znajdą się najbardziej czarne elementy zdjęcia
        for (int x = 0; x < imgCopy.cols(); x++) {
            for (int y = 0; y < imgCopy.rows(); y++) {
                double[] val = imgCopy.get(y, x);
                if (Arrays.stream(val).anyMatch(v -> v < colorThreshold)) {
                    if (up > y) up = y;
                    if (down < y) down = y;
                    if (left > x) left = x;
                    if (right < x) right = x;
                }
            }
        }
        Rect rect = new Rect(new Point(left, up), new Point(right, down));
        return source.submat(rect);
    }

    private void removeTemplateFromImg(Mat source, Mat template, Point maxMatchLoc, Scalar color) {
        MatOfPoint rect = rectContourFromPoints(0,0, maxMatchLoc.x + template.cols(), maxMatchLoc.y + template.rows());
        fillPoly(source, Collections.singletonList(rect), color);
    }

    public static Mat loadImage(String imagePath) {
        return Imgcodecs.imread(imagePath, IMREAD_COLOR);
    }

    public static void saveImage(Mat source, String imagePath) {
        Imgcodecs.imwrite(imagePath, source);
    }

    public Mat bytes2Mat(byte[] bytes) {
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
    }

    public Mat reshape(Mat source, int channels, int rows, int columns){
        return source.reshape(channels, new int[]{rows, columns});
    }

    private void fillPoly(Mat source, List<MatOfPoint> poly, Scalar color) {
        Imgproc.fillPoly(source, poly, color);
    }

    public Mat from2dArray1Channel(int[][] array, int rows, int columns, int type) {
        Mat mat = new Mat();
        mat.create(rows, columns, type);
        for (int y=0; y<rows; y++) {
            for (int x=0; x<columns; x++) {
                mat.put(y, x, array[y][x]);
            }
        }

        return mat;
    }

    public Mat convertToColorScale(Mat source, int colorScale) {
        Mat changedImage = new Mat();
        Imgproc.cvtColor(source, changedImage, colorScale);
        return changedImage;
    }

    public MatOfPoint rectContourFromPoints(double x, double y, double w, double h) {
        List<Point> rectEdges = Arrays.asList(new Point(x, y),
                                              new Point(x + w, y),
                                              new Point(x + w, y + h),
                                              new Point(x, y + h));
        return contourFromPoints(rectEdges);
    }

    private MatOfPoint contourFromPoints(List<Point> points) {
        MatOfPoint contour = new MatOfPoint();
        contour.fromList(points);
        return contour;
    }

    private Mat findTemplateInImg(Mat source, Mat template, int method) {
        Mat result = new Mat();
        Imgproc.matchTemplate(source, template, result, method);
        return result;
    }
}