package djl.utils;

import ai.djl.Device;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import djl.utils.options.TrainArgumentOptions;
import lombok.Getter;
import org.apache.commons.cli.*;

import java.lang.reflect.Type;
import java.util.Map;

import static djl.utils.options.TrainArgumentOptions.*;

@Getter
public class TrainArguments {

    private int epoch;
    private int batchSize;
    private int maxGpus;
    private boolean isSymbolic;
    private boolean preTrained;
    private String outputDir;
    private long limit;
    private String modelDir;
    private Map<String, String> criteria;

    private String testDatasetPath;
    private String trainDatasetPath;

    private String modelName;

    private int imageHeight;
    private int imageWidth;
    private int imageChannels;

    public TrainArguments(CommandLine cmd) {
        if (cmd.hasOption(EPOCH.getLongOpt())) {
            epoch = Integer.parseInt(cmd.getOptionValue(EPOCH.getLongOpt()));
        } else {
            epoch = 2;
        }
        maxGpus = Device.getGpuCount();
        if (cmd.hasOption(MAX_GPUS.getLongOpt())) {
            maxGpus = Math.min(Integer.parseInt(cmd.getOptionValue(MAX_GPUS.getLongOpt())), maxGpus);
        }
        if (cmd.hasOption(BATCH_SIZE.getLongOpt())) {
            batchSize = Integer.parseInt(cmd.getOptionValue(BATCH_SIZE.getLongOpt()));
        } else {
            batchSize = maxGpus > 0 ? 32 * maxGpus : 32;
        }
        isSymbolic = cmd.hasOption(SYMBOLIC.getLongOpt());
        preTrained = cmd.hasOption(PRE_TRAINED.getLongOpt());

        if (cmd.hasOption(OUTPUT_DIR.getLongOpt())) {
            outputDir = cmd.getOptionValue(OUTPUT_DIR.getLongOpt());
        } else {
            outputDir = "build/model";
        }
        if (cmd.hasOption(MAX_BATCHES.getLongOpt())) {
            limit = Long.parseLong(cmd.getOptionValue(MAX_BATCHES.getLongOpt())) * batchSize;
        } else {
            limit = Long.MAX_VALUE;
        }
        if (cmd.hasOption(MODEL_DIR.getLongOpt())) {
            modelDir = cmd.getOptionValue(MODEL_DIR.getLongOpt());
        } else {
            modelDir = null;
        }
        if (cmd.hasOption(CRITERIA.getLongOpt())) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            criteria = new Gson().fromJson(cmd.getOptionValue(CRITERIA.getLongOpt()), type);
        }
        if (cmd.hasOption(TEST_PATH.getLongOpt())) {
            testDatasetPath = cmd.getOptionValue(TEST_PATH.getLongOpt());
        }
        if (cmd.hasOption(TRAIN_PATH.getLongOpt())) {
            trainDatasetPath = cmd.getOptionValue(TRAIN_PATH.getLongOpt());
        }
        if (cmd.hasOption(MODEL_NAME.getLongOpt())) {
            modelName = cmd.getOptionValue(MODEL_NAME.getLongOpt());
        } else {
            modelName = "model";
        }
        if (cmd.hasOption(IMAGE_HEIGHT.getLongOpt())) {
            imageHeight = Integer.parseInt(cmd.getOptionValue(IMAGE_HEIGHT.getLongOpt()));
        }
        if (cmd.hasOption(IMAGE_WIDTH.getLongOpt())) {
            imageWidth = Integer.parseInt(cmd.getOptionValue(IMAGE_WIDTH.getLongOpt()));
        }
        if (cmd.hasOption(IMAGE_CHANNELS.getLongOpt())) {
            imageChannels = Integer.parseInt(cmd.getOptionValue(IMAGE_CHANNELS.getLongOpt()));
        }
    }

    public static TrainArguments parseArgs(String[] args) throws ParseException {
        Options options = TrainArguments.getOptions();
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, null, false);
        return new TrainArguments(cmd);
    }

    public static Options getOptions() {
        Options options = new Options();
        for (TrainArgumentOptions trainOption : TrainArgumentOptions.values()) {
            options.addOption(
                    Option.builder(trainOption.getOpt())
                            .longOpt(trainOption.getLongOpt())
                            .hasArg()
                            .argName(trainOption.getArgName())
                            .desc(trainOption.getDesc())
                            .build());
        }

        return options;
    }

    public int getImagePixels() {
        return imageHeight * imageWidth * imageChannels;
    }
}