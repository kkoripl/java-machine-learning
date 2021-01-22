package djl.objectDetection;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.TranslateException;
import djl.objectDetection.dto.ObjectDetectionDto;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

public class ObjectDetectionService {

    @Resource(name="predictorProvider")
    private Supplier<Predictor<Image, DetectedObjects>> autoconfigPredictorProvider;
    private final Predictor<Image, DetectedObjects> externalPredictor;

    public ObjectDetectionService(Predictor<Image, DetectedObjects> externalPredictor) {
        this.externalPredictor = externalPredictor;
    }

    public ObjectDetectionDto autoConfigDetectObjects(MultipartFile file) throws TranslateException, IOException {
        return detectObjects(file, autoconfigPredictorProvider.get());
    }

    public ObjectDetectionDto externalDetectObjects(MultipartFile file) throws IOException, TranslateException {
        return detectObjects(file, externalPredictor);
    }

    private ObjectDetectionDto detectObjects(MultipartFile imageFile, Predictor<Image, DetectedObjects> predictor) throws TranslateException, IOException {
        BufferedImage bfImage = bytes2BufferedImage(imageFile.getBytes());
        Image img = BufferedImageFactory.getInstance().fromImage(bfImage);
        DetectedObjects detectionResult = predictor.predict(img);
        ObjectDetectionDto results = prepareResults(detectionResult);
        results.setImgBytes(bufferedImage2Bytes(drawBoundingBoxImage(img, detectionResult), imageFile.getContentType()));
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

    private BufferedImage bytes2BufferedImage(byte[] bytes) throws IOException {
        BufferedImage originalBf = ImageIO.read(new ByteArrayInputStream(bytes));
        return copyToWorkWithOpenJdk(originalBf);

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
