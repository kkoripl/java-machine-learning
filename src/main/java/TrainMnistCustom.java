import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.ImageFolder;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.repository.Repository;
import ai.djl.training.*;
import ai.djl.training.dataset.Batch;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.*;
import ai.djl.training.loss.Loss;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import org.apache.commons.cli.ParseException;
import utils.ImageProperties;

import java.io.IOException;
import java.nio.file.Paths;

public final class TrainMnistCustom {

    private static final int CLASSES_CNT = 10; // liczba wyjść = liczba klas
    private static final int MNIST_IMG_HEIGHT = 28; // ostateczny rozmiar obrazka, po przygotowaniu
    private static final int MNIST_IMG_WIDTH = 28;
    private static final int MNIST_IMG_CHANNELS = 3;
    private static final int EPOCHS = 2; // liczba epok uczenia
    private static final int BATCH_SIZE = 32; // liczba obrazków w batchu uczenia

    public static void main(String[] args) throws IOException, ParseException {
        Arguments arguments = Arguments.parseArgs(args);
        ImageProperties imageProperties = new ImageProperties(MNIST_IMG_WIDTH, MNIST_IMG_HEIGHT, MNIST_IMG_CHANNELS);
        buildModel(arguments, imageProperties);
    }

    private static void buildModel(Arguments arguments, ImageProperties imageProperties) throws IOException {
        // folder z obrazkami musi być pod ścieżką: root/klasa/obrazki - inaczej powie, ze nie widzi folderu ni obrazków - nazwa podfolderu nie może zaczynać się od kropki!
        ImageFolder trainDataset = prepareDataset(arguments.getTrainDatasetPath(), arguments.getBatchSize());

        System.out.println(trainDataset.size());
        // synset to klasy - Synset instances are the groupings of synonymous words that express the same concept. Some of the words have only one Synset and some have several.
        RandomAccessDataset[] sets = trainDataset.randomSplit(75, 25);

        Model model = Model.newInstance("mnistCustomModel");

        for (Batch batch : trainDataset.getData(NDManager.newBaseManager())) {
            // head pobiera pierwszy NDArray
            System.out.println(batch.getLabels().head());
            // trzeba zamknąć batch po jego wykorzystaniu
            batch.close();
        }
        model.setBlock(setupNeuralNetwork(imageProperties, CLASSES_CNT, new int[]{128, 64}));
        Trainer trainer = setupTrainer(model, arguments, imageProperties);
        EasyTrain.fit(trainer, EPOCHS, sets[0], sets[1]);
        model.save(Paths.get(arguments.getOutputDir()), "mnistCustom");
        System.out.println(trainer.getTrainingResult().getEvaluations());
    }

    private static ImageFolder prepareDataset(String datasetPath, int batchSize) throws IOException {
        ImageFolder dataset = ImageFolder.builder()
                .optLimit(Long.MAX_VALUE)
                .setRepository(Repository.newInstance("mnistCustom", Paths.get(datasetPath)))
                .optPipeline(preprocessImages(MNIST_IMG_WIDTH, MNIST_IMG_HEIGHT))
                .setSampling(batchSize, true) // dzielenie danych na batche
                .build();

        dataset.prepare();
        System.out.println(dataset.getSynset());

        return dataset;
    }

    private static Pipeline preprocessImages(int imageWidth, int imageHeight) {
        return new Pipeline()
                .add(new Resize(imageWidth, imageHeight))
                .add(new ToTensor());
    }

    private static Trainer setupTrainer(Model model, Arguments arguments, ImageProperties imageProperties) {
        Trainer trainer = model.newTrainer(setupTrainingConfig(arguments));
        trainer.setMetrics(new Metrics());
        Shape inputShape = new Shape(imageProperties.getHeight(), imageProperties.getWidth(), imageProperties.getChannels());
        trainer.initialize(inputShape);
        return trainer;
    }

    private static TrainingConfig setupTrainingConfig(Arguments arguments) {
        String outputDir = arguments.getOutputDir();
        return new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optDevices(Device.getDevices(arguments.getMaxGpus())) // nastawienie procesora - ile max GPU mamy, jak wyjdzie po sprawdzeniu, że zero, to CPU
                .addTrainingListeners(new LoggingTrainingListener()) // progresss bar nauki i walidacji - bez metryk
                .addTrainingListeners(new EvaluatorTrainingListener()) // dodaje metryki do progress bara + uruchamia walidację
                .addTrainingListeners(new TimeMeasureTrainingListener(outputDir)) // dokłada szybkosc: liczbę itemow / sek
                .addTrainingListeners(new EpochTrainingListener())
//                .addTrainingListeners(TrainingListener.Defaults.logging(outputDir))

                .addTrainingListeners(new MemoryTrainingListener(outputDir))
//                .addTrainingListeners(new DivergenceCheckTrainingListener())

                .addTrainingListeners(setupTrainingListener(outputDir));
    }

    private static TrainingListener setupTrainingListener(String outputDir) {
        CheckpointsTrainingListener listener = new CheckpointsTrainingListener(outputDir);
        listener.setSaveModelCallback( // nastawienie co ma się zrobić na koniec treningu z modelem przed jego zapisem (model.save)
                trainer -> {
                    TrainingResult result = trainer.getTrainingResult();
                    Model model = trainer.getModel();
                    float accuracy = result.getValidateEvaluation("Accuracy");
                    model.setProperty("Accuracy", String.format("%.5f", accuracy)); //cechy modelu - tj. jego włąsciwosci do odtworzenia
                    model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
                });
        return listener;
    }

    private static Block setupNeuralNetwork(ImageProperties imageProperties, int classes, int[] neuronsInHiddenLayers) {
        //MLP - MultiLayerPerceptron
        return new Mlp(imageProperties.getPixelsCount(), // liczba neuronow wejsciowych
                        classes, // liczba neuronow wyjsciowych
                        neuronsInHiddenLayers); // liczba neuronow w warstawach ukrytych
    }

}
