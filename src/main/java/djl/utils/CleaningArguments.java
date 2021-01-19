package djl.utils;

import djl.utils.options.CleanArgumentOptions;
import lombok.Getter;
import org.apache.commons.cli.*;

import static djl.utils.options.CleanArgumentOptions.*;

@Getter
public class CleaningArguments {
    private String imageDatasetPath;
    private String csvDatasetPath;
    private String finalDatasetPath;
    private String unwantedPartPath;

    private double similarityThreshold;

    private String labelColumn;
    private String imageColumn;

    private int imageWidth;
    private int imageHeight;
    private int joinedIAreaPx;
    private double colorThreshold;

    public CleaningArguments(CommandLine cmd) {
        if (cmd.hasOption(IMAGE_PATH.getLongOpt())) {
            imageDatasetPath = cmd.getOptionValue(IMAGE_PATH.getLongOpt());
        }
        if (cmd.hasOption(CSV_PATH.getLongOpt())) {
            csvDatasetPath = cmd.getOptionValue(CSV_PATH.getLongOpt());
        }
        if (cmd.hasOption(FINAL_PATH.getLongOpt())) {
            finalDatasetPath = cmd.getOptionValue(FINAL_PATH.getLongOpt());
        }
        if (cmd.hasOption(UNWANTED_PART_PATH.getLongOpt())) {
            unwantedPartPath = cmd.getOptionValue(UNWANTED_PART_PATH.getLongOpt());
        }
        if (cmd.hasOption(UNWANTED_SIMILARITY.getLongOpt())) {
            similarityThreshold = Double.parseDouble(cmd.getOptionValue(UNWANTED_SIMILARITY.getLongOpt()));
        }
        if (cmd.hasOption(IMAGE_COLUMN.getLongOpt())) {
            imageColumn = cmd.getOptionValue(IMAGE_COLUMN.getLongOpt());
        }
        if (cmd.hasOption(LABEL_COLUMN.getLongOpt())) {
            labelColumn = cmd.getOptionValue(LABEL_COLUMN.getLongOpt());
        }
        if (cmd.hasOption(LABEL_COLUMN.getLongOpt())) {
            imageWidth = Integer.parseInt(cmd.getOptionValue(IMAGE_WIDTH.getLongOpt()));
        }
        if (cmd.hasOption(LABEL_COLUMN.getLongOpt())) {
            imageHeight = Integer.parseInt(cmd.getOptionValue(IMAGE_HEIGHT.getLongOpt()));
        }
        if (cmd.hasOption(LABEL_COLUMN.getLongOpt())) {
            joinedIAreaPx = Integer.parseInt(cmd.getOptionValue(JOINED_I_AREA_PX.getLongOpt()));
        }
        if (cmd.hasOption(LABEL_COLUMN.getLongOpt())) {
            colorThreshold = Double.parseDouble(cmd.getOptionValue(COLOR_THRESHOLD.getLongOpt()));
        }
    }

    public static CleaningArguments parseArgs(String[] args) throws ParseException {
        Options options = CleaningArguments.getOptions();
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, null, false);
        return new CleaningArguments(cmd);
    }

    public static Options getOptions() {
        Options options = new Options();
        for (CleanArgumentOptions cleanOption : CleanArgumentOptions.values()) {
            options.addOption(
                    Option.builder(cleanOption.getOpt())
                            .longOpt(cleanOption.getLongOpt())
                            .hasArg()
                            .argName(cleanOption.getArgName())
                            .desc(cleanOption.getDesc())
                            .build());
        }

        return options;
    }
}