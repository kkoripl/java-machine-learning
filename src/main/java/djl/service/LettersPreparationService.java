package djl.service;

import djl.handwritting.service.LettersDivider;
import lombok.RequiredArgsConstructor;
import org.opencv.core.Mat;

import java.util.List;

@RequiredArgsConstructor
public class LettersPreparationService {

    private final LettersDivider lettersDivider;

    List<Mat> prepareLetterImages(byte[] imgBytes) {
        return lettersDivider.divideLetters(imgBytes);
    }
}
