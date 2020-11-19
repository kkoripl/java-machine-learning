package handwritting;

import handwritting.preparators.DatasetCleaner;
import handwritting.utils.CleaningArguments;
import handwritting.utils.opencv.OpenCVWrapper;

import java.nio.file.Paths;
import java.util.Arrays;

public class Cleaning {
    public static void main(String[] args) throws Exception {
        CleaningArguments arguments = CleaningArguments.parseArgs(args);

        OpenCVWrapper.loadLibrary();

        DatasetCleaner cleanerTrain = DatasetCleaner.builder()
                .csvDatasetPath(Paths.get(arguments.getCsvDatasetPath()).toString())
                .imageDatasetPath(Paths.get(arguments.getImageDatasetPath()).toString())
                .lettersDatasetPath(Paths.get(arguments.getFinalDatasetPath()).toString())
                .imageColumn(arguments.getImageColumn())
                .labelColumn(arguments.getLabelColumn())
                .unwantedLabels(Arrays.asList("EMPTY", "UNREADABLE", ""))
                .unwantedSigns(Arrays.asList("'","-"," ", "`"))
                .unwantedSimilarityThreshold(arguments.getSimilarityThreshold())
                .unwantedPartPath(arguments.getUnwantedPartPath())
                .build();

        cleanerTrain.cleanDataset();
    }
}