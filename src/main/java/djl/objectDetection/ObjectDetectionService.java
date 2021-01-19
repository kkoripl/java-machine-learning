package djl.objectDetection;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.Artifact;
import ai.djl.translate.TranslateException;
import djl.objectDetection.dto.ObjectDetectionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

public class ObjectDetectionService {

    @Resource
    private Supplier<Predictor<Image, DetectedObjects>> predictorProvider;

    public ObjectDetectionDto detectObjects(MultipartFile file) throws TranslateException, IOException {
        BufferedImage image = bytes2BuferedImage(file.getBytes());
        Image img = BufferedImageFactory.getInstance().fromImage(image);
        DetectedObjects detectionResult  = predictorProvider.get().predict(img);
        ObjectDetectionDto results = prepareResults(detectionResult);
        results.setImgBytes(bufferedImage2Bytes(drawBoundingBoxImage(img, detectionResult), file.getContentType()));

        return results;
    }

    private ObjectDetectionDto prepareResults(DetectedObjects detections) {
        ObjectDetectionDto results = new ObjectDetectionDto();
        for (Classifications.Classification detection : detections.items()) {
            results.addProbability(detection.getProbability());
            results.addClass(detection.getClassName());
        }
        return results;
    }

    private BufferedImage drawBoundingBoxImage(Image image, DetectedObjects detections) {
        ai.djl.modality.cv.Image newImg = image.duplicate(Image.Type.TYPE_INT_ARGB);
        newImg.drawBoundingBoxes(detections);
        return (BufferedImage) newImg.getWrappedImage();
    }

    private BufferedImage bytes2BuferedImage(byte[] bytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(bytes));

    }

    private byte[] bufferedImage2Bytes(BufferedImage image, String contentType) throws IOException {
        BufferedImage newImage = copyToWorkWithOpenJdk(image);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(newImage, getImageExtFromContent(contentType), bos);
        return bos.toByteArray();
    }

    private BufferedImage copyToWorkWithOpenJdk(BufferedImage originalImage) {
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] rgb = originalImage.getRGB(0, 0, w, h, null, 0, w);
        newImage.setRGB(0, 0, w, h, rgb, 0, w);
        return newImage;
    }

    private String getImageExtFromContent(String contentType) {
         return contentType.substring(contentType.indexOf("/")+1);
    }
}
