package djl.handwritting.translator;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import lombok.AllArgsConstructor;
import org.opencv.core.Mat;

import java.util.List;

import djl.handwritting.utils.*;

@AllArgsConstructor
public class HandwrittingTranslator implements Translator<Mat, Classifications> {

    private List<String> classes;

    @Override
    public NDList processInput(TranslatorContext ctx, Mat input) {
        NDArray array = (new MatImageWrapper(input)).toNDArray(ctx.getNDManager(), Image.Flag.GRAYSCALE);
        return new NDList(NDImageUtils.toTensor(array));
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) {
        // Funkcja wygładzająca prawdopodobieństwo - ustawienie jednego wyniku na raz
        NDArray probabilities = list.singletonOrThrow().softmax(0);
        // Nastawienie faktycznych nazw klas
        return new Classifications(classes, probabilities);
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}
