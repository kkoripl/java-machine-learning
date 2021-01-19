package demo;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.Mnist;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.CheckpointsTrainingListener;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.metric.Metrics;
import ai.djl.training.util.ProgressBar;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import djl.utils.Arguments;

public final class TrainMnist {

    private TrainMnist() {}

    public static void main(String[] args) throws IOException, ParseException, TranslateException, MalformedModelException {
//        demo.TrainMnist.runExample(args);
        TrainMnist.classify();
    }

    public static void classify() throws IOException, MalformedModelException, TranslateException {
        Image img = ImageFactory.getInstance().fromUrl("https://djl-ai.s3.amazonaws.com/resources/images/0.png");
        img.getWrappedImage();
        Path modelDir = Paths.get("mlp_model");
        /** Zapisywane sa tylko wewnetrzne parametry modelu, bez jego struktury, dlatego trzeba ponownie wskazac, co budujemy i jaki jest jego szkielet **/
        Model model = Model.newInstance("mlp");
        model.setBlock(new Mlp(28 * 28,10, new int[]{128, 64}));
        model.load(modelDir);

        Translator<Image, Classifications> translator = new Translator<Image, Classifications>() {
            //pre i post processing danych oraz wyników

            // Preprocessing danych
            @Override
            public NDList processInput(TranslatorContext translatorContext, Image image) throws Exception {
                // Obraz do NDArraya, nastawa o odcieniu kolorów - potencjalnie dla innych danych, co innego
                NDArray array = image.toNDArray(translatorContext.getNDManager(), Image.Flag.GRAYSCALE);
                return new NDList(NDImageUtils.toTensor(array));
            }

            // Postprocessing wyników
            @Override
            public Classifications processOutput(TranslatorContext translatorContext, NDList ndList) throws Exception {
                // Funkcja wygładzająca prawdopodobieństwo - ustawienie jednego wyniku na raz
                NDArray probabilities = ndList.singletonOrThrow().softmax(0);
                // Nastawienie faktycznych nazw klas
                List<String> indicies = IntStream.range(0, 10).mapToObj(String::valueOf).collect(Collectors.toList());
                return new Classifications(indicies, probabilities);
            }

            // Sposób tworzenia batchów do klasyfikacji - STACK, każdy element oddzielnie?
            @Override
            public Batchifier getBatchifier() {
                return Batchifier.STACK;
            }
        };

        Predictor<Image, Classifications> predictor = model.newPredictor(translator);
        Classifications classifications = predictor.predict(img);
        System.out.println(classifications.toString());
    }

    public static TrainingResult runExample(String[] args) throws IOException, ParseException {
        Arguments arguments = Arguments.parseArgs(args);

        // Construct neural network
        Block block =
                new Mlp(
                        Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH, // liczba neuronow wejsciowych
                        Mnist.NUM_CLASSES, // liczba neuronow wyjsciowych
                        new int[] {128, 64}); // liczba neuronow w warstawach ukrytych

        try (Model model = Model.newInstance("mlp")) {
            model.setBlock(block);

            // get training and validation dataset
            RandomAccessDataset trainingSet = getDataset(Dataset.Usage.TRAIN, arguments);
            RandomAccessDataset validateSet = getDataset(Dataset.Usage.TEST, arguments);

            // setup training configuration
            DefaultTrainingConfig config = setupTrainingConfig(arguments);

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.setMetrics(new Metrics());

                /*
                 * MNIST is 28x28 grayscale image and pre processed into 28 * 28 NDArray.
                 * 1st axis is batch axis, we can use 1 for initialization.
                 */
                Shape inputShape = new Shape(1, Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH);

                // initialize trainer with proper input shape
                trainer.initialize(inputShape);

                EasyTrain.fit(trainer, arguments.getEpoch(), trainingSet, validateSet);

                return trainer.getTrainingResult();
            }
        }
    }

    private static DefaultTrainingConfig setupTrainingConfig(Arguments arguments) {
        String outputDir = arguments.getOutputDir();
        CheckpointsTrainingListener listener = new CheckpointsTrainingListener(outputDir);
        listener.setSaveModelCallback(
                trainer -> {
                    TrainingResult result = trainer.getTrainingResult();
                    Model model = trainer.getModel();
                    float accuracy = result.getValidateEvaluation("Accuracy");
                    model.setProperty("Accuracy", String.format("%.5f", accuracy));
                    model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
                });
        return new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optDevices(Device.getDevices(arguments.getMaxGpus()))
                .addTrainingListeners(TrainingListener.Defaults.logging(outputDir))
                .addTrainingListeners(listener);
    }

    private static RandomAccessDataset getDataset(Dataset.Usage usage, Arguments arguments)
            throws IOException {
        Mnist mnist =
                Mnist.builder()
                        .optUsage(usage)
                        .setSampling(arguments.getBatchSize(), true)
                        .optLimit(arguments.getLimit())
                        .build();
        mnist.prepare(new ProgressBar());
        return mnist;
    }
}

