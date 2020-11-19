package handwritting.utils.opencv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opencv.core.Scalar;

@Getter
@AllArgsConstructor
public enum OpenCvColor {
    WHITE(new Scalar(255,255,255)),
    RED(new Scalar(255,0,0));

    Scalar scalar;
}