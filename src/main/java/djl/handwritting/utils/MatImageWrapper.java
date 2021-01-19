package djl.handwritting.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Joints;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import djl.utils.opencv.OpenCVWrapper;
import lombok.RequiredArgsConstructor;
import org.opencv.core.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class MatImageWrapper implements Image {

    private final Mat image;
    private String path = "";

    @Override
    public int getWidth() {
        return image.width();
    }

    @Override
    public int getHeight() {
        return image.height();
    }

    @Override
    public Object getWrappedImage() {
        return image;
    }

    @Override
    public Image getSubimage(int x, int y, int w, int h) {
        Point leftUpperCorner = new Point(x, y);
        Size size = new Size();
        Rect roi = new Rect(leftUpperCorner, size);
        return new MatImageWrapper(new Mat(image, roi));
    }

    @Override
    public Image duplicate(Type type) {
        Mat newMat = new Mat();
        image.copyTo(newMat);
        return new MatImageWrapper(newMat);
    }

    @Override
    public NDArray toNDArray(NDManager manager, Flag flag) {
        int width = image.width();
        int height = image.height();
        int channel;
        if (flag == Flag.GRAYSCALE) {
            channel = 1;
        } else {
            channel = 3;
        }

        ByteBuffer bb = manager.allocateDirect(channel * height * width);
        for (int row=0; row<image.rows(); row++ ) {
            for (int col=0; col<image.cols(); col++) {
                double[] vals = image.get(row, col);
                bb.put((byte)vals[0]);
                if (flag != Flag.GRAYSCALE) {
                    bb.put((byte)vals[1]);
                    bb.put((byte)vals[2]);
                }
            }
        }
        bb.rewind(); // wracamy w buforze na pozycję 0
        return manager.create(bb, new Shape(height, width, channel), DataType.UINT8);
    }

    public void saveImage(OutputStream os, String path, String type) {
        this.path = path;
        save(os, type);
    }

    @Override
    public void save(OutputStream os, String type) {
        Mat mat = new MatOfByte(streamToBytes(os));
        OpenCVWrapper.saveImage(mat, path);
    }

    @Override
    public void drawBoundingBoxes(DetectedObjects detections) {

    }

    @Override
    public void drawJoints(Joints joints) {

    }

    private static byte[] streamToBytes(OutputStream stream) {
        return ((ByteArrayOutputStream) stream).toByteArray();
    }
}
