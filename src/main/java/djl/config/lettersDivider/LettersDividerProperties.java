package djl.config.lettersDivider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LettersDividerProperties {
    LettersColorProperties color;
    LettersBinarizedProperties binarized;
    int joinedIAreaPx;
    String templateFilePath;
    double templateSimilarityThreshold;
    int imgWidth;
    int imgHeight;
}
