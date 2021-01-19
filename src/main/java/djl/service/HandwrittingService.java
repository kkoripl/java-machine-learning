package djl.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.translate.TranslateException;
import lombok.RequiredArgsConstructor;
import org.opencv.core.Mat;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class HandwrittingService {

    private final LettersPreparationService lettersPreparationService;
    private final Predictor<Mat, Classifications> handwrittingPredictor;

    public String predict(MultipartFile file) throws TranslateException, IOException {
        List<Mat> letterImgs = lettersPreparationService.prepareLetterImages(file.getBytes());
        StringBuilder text = new StringBuilder();

        for (Mat letterImg : letterImgs) {
            Classifications classifications = handwrittingPredictor.predict(letterImg);
            Classifications.Classification best = classifications.topK(1).get(0);
            text.append(best.getClassName());
        }

        return text.toString();
    }
}
