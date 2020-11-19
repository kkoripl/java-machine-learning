package handwritting;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import handwritting.dto.BaseMlpModelFeatures;
import handwritting.preparators.LettersDivider;
import handwritting.preparators.MatImageWrapper;
import handwritting.utils.opencv.OpenCVWrapper;
import org.opencv.core.Mat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Classifying {
    public static void main(String[] args) throws Exception {
        String imageFilePath = Paths.get("src/main/data/hand-writting-dataset/test_v2/test/TEST_0001.jpg").toString();
        String templateFilePath = Paths.get("src/main/resources/test/nomTemplate.png").toString();

        BaseMlpModelFeatures features = prepareModelFeatures();
        Model model = prepareModel(features);
        String text = predictText(model, features.getClasses(), imageFilePath, templateFilePath);
        System.out.println(text);
    }

    private static String predictText(Model model, List<String> classes, String imageFilePath, String templateFilePath) throws TranslateException {
        List<Mat> letters = prepareLetterImages(imageFilePath, templateFilePath);
        Predictor<Mat, Classifications> predictor = model.newPredictor(prepareTranslator(classes));
        return predict(predictor, letters);
    }

    private static Translator<Mat, Classifications> prepareTranslator(List<String> classes) {
        return new Translator<Mat, Classifications>() {
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
        };
    }

    private static BaseMlpModelFeatures prepareModelFeatures() {
        String modelPath = "mlp_model";
        String modelName = "letters001";
        Shape inputShape = new Shape(28, 28, 1);
        int[] hiddenLayersNeurons = new int[]{400, 300, 150};
        List<String> classes = Arrays.asList("A", "B", "C", "D", "E", "F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z");

        return BaseMlpModelFeatures.builder()
                .modelName(modelName)
                .modelPath(modelPath)
                .inputShape(inputShape)
                .hiddenLayersNeurons(hiddenLayersNeurons)
                .classes(classes)
                .build();
    }

    private static Model prepareModel(BaseMlpModelFeatures features) throws IOException, MalformedModelException {
        Model model = Model.newInstance(features.getModelName());
        model.setBlock(new Mlp(features.getInputSize(), features.getOutputSize(), features.getHiddenLayersNeurons()));
        model.load(Paths.get(features.getModelPath()));
        return model;
    }

    private static List<Mat> prepareLetterImages(String imageFilePath, String templateFilePath) {
        OpenCVWrapper.loadLibrary();
        Mat img = OpenCVWrapper.loadImage(imageFilePath);
        Mat template = OpenCVWrapper.loadImage(templateFilePath);
        img = OpenCVWrapper.cleanImage(img, template, 0.65);
        return LettersDivider.findLetters(img);
    }

    private static String predict(Predictor<Mat, Classifications> predictor, List<Mat> letters) throws TranslateException {
        StringBuilder text = new StringBuilder();

        for (Mat letter : letters) {
            Classifications classifications = predictor.predict(letter);
            Classifications.Classification best = classifications.topK(1).get(0);
            text.append(best.getClassName());
        }

        return text.toString();
    }


}
