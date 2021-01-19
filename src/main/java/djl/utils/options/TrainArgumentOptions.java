package djl.utils.options;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TrainArgumentOptions {
    EPOCH("e", "epoch", "EPOCH", "Numbers of epochs user would like to run"),
    BATCH_SIZE("b", "batch-size", "BATCH-SIZE", "The batch size of the training data."),
    MAX_GPUS("g", "max-gpus", "MAXGPUS", "Max number of GPUs to use for training"),
    SYMBOLIC("s", "symbolic-model", "SYMBOLIC", "Use symbolic model, use imperative model if false"),
    PRE_TRAINED("p", "pre-trained", "PRE-TRAINED", "Use pre-trained weights"),
    OUTPUT_DIR("o", "output-dir", "OUTPUT-DIR", "Use output to determine directory to save your model parameters"),
    MAX_BATCHES("m", "max-batches", "max-batches", "Limit each epoch to a fixed number of iterations to test the training script"),
    MODEL_DIR("d", "model-dir", "MODEL-DIR", "pre-trained model file directory"),
    CRITERIA("r", "criteria", "CRITERIA", "The criteria used for the model."),
    TEST_PATH("tst", "test-path", "TEST-DATASET-PATH", "Test dataset path"),
    TRAIN_PATH("trn", "train-path", "TRAIN-DATASET-PATH", "Train dataset path"),
    MODEL_NAME("mod", "model-name", "MODEL-NAME", "Created model name"),
    IMAGE_WIDTH("iw", "image-width", "IMAGE-WIDTH", "Image width in pixels"),
    IMAGE_HEIGHT("ih", "image-height", "IMAGE-HEIGHT", "Image height in pixels"),
    IMAGE_CHANNELS("ic", "image-channels", "IMAGE-CHANNELS", "Image channels");

    private String opt;
    private String longOpt;
    private String argName;
    private String desc;
}