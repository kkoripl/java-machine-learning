package djl.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.translate.TranslateException;
import lombok.RequiredArgsConstructor;
import org.opencv.core.Mat;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import djl.handwritting.dto.HandwrittingClassificationDto;

@RequiredArgsConstructor
public class HandwrittingService {

    private final LettersPreparationService lettersPreparationService;
    private final Predictor<Mat, Classifications> handwrittingPredictor;

    public HandwrittingClassificationDto predict(MultipartFile file) throws TranslateException, IOException {
        List<Mat> letterImgs = lettersPreparationService.prepareLetterImages(file.getBytes());
        StringBuilder text = new StringBuilder();
        HandwrittingClassificationDto handWrittingDto = new HandwrittingClassificationDto();

        for (Mat letterImg : letterImgs) {
            Classifications classifications = handwrittingPredictor.predict(letterImg);
            Classifications.Classification best = classifications.topK(1).get(0);
            handWrittingDto.addProbability(best.getProbability());
            handWrittingDto.addClass(best.getClassName());
            text.append(best.getClassName());
        }

        handWrittingDto.setText(text.toString());
        return handWrittingDto;
    }
}
