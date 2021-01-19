package djl.scripts.training;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.basicdataset.ImageFolder;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Block;
import ai.djl.repository.Repository;
import ai.djl.training.*;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.*;
import ai.djl.training.loss.Loss;
import djl.utils.TrainArguments;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Function;

public class Training {
    public static void main(String[] args) throws Exception {

        TrainArguments arguments = TrainArguments.parseArgs(args);

        ImageFolder trainSet = prepareDataset(arguments.getTrainDatasetPath(), arguments.getBatchSize(), "trainDataset");
        ImageFolder testSet = prepareDataset(arguments.getTestDatasetPath(), arguments.getBatchSize(), "testDataset");

        Model model = Model.newInstance(arguments.getModelName());
        model.setBlock(buildMlp(arguments.getImagePixels(), trainSet.getSynset().size(), new int[]{400, 300, 150}, Activation::relu));
        Trainer trainer = setupTrainer(model, arguments);
        EasyTrain.fit(trainer, arguments.getEpoch(), trainSet, testSet);
        model.save(Paths.get(arguments.getOutputDir()), arguments.getModelName());
        System.out.println(trainer.getTrainingResult().getEvaluations());

    }

    private static ImageFolder prepareDataset(String datasetPath, int batchSize, String repositoryName) throws IOException {
        ImageFolder dataset = ImageFolder.builder()
                .optLimit(Long.MAX_VALUE)
                .optFlag(Image.Flag.GRAYSCALE)
                .setRepository(Repository.newInstance(repositoryName, Paths.get(datasetPath)))
                .setSampling(batchSize, true) // dzielenie danych na batche
                .build();

        dataset.prepare();
        return dataset;
    }


    private static Block buildMlp(int inputNeurons, int outputNeurons, int[] hiddenLayersNeurons, Function<NDList, NDList> activation) {
        return new Mlp(inputNeurons,
                       outputNeurons,
                       hiddenLayersNeurons,
                       activation);
    }

    private static Trainer setupTrainer(Model model, TrainArguments arguments) {
        Trainer trainer = model.newTrainer(setupTrainingConfig(arguments));
        trainer.setMetrics(new Metrics());
        Shape inputShape = new Shape(arguments.getImageHeight(), arguments.getImageWidth(), arguments.getImageChannels());
        trainer.initialize(inputShape);
        return trainer;
    }

    private static TrainingConfig setupTrainingConfig(TrainArguments arguments) {
        String outputDir = arguments.getOutputDir();
        return new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optDevices(Device.getDevices(arguments.getMaxGpus())) // nastawienie procesora - ile max GPU mamy, jak wyjdzie po sprawdzeniu, że zero, to CPU
                .addTrainingListeners(new LoggingTrainingListener()) // progresss bar nauki i walidacji - bez metryk
                .addTrainingListeners(new EvaluatorTrainingListener()) // dodaje metryki do progress bara + uruchamia walidację
                .addTrainingListeners(new TimeMeasureTrainingListener(outputDir)) // dokłada szybkosc: liczbę itemow / sek
                .addTrainingListeners(new EpochTrainingListener())
                .addTrainingListeners(TrainingListener.Defaults.logging(outputDir))

                .addTrainingListeners(new MemoryTrainingListener(outputDir))
                .addTrainingListeners(new DivergenceCheckTrainingListener())

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
}
