package djl.scripts.preparation;

import djl.handwritting.cleaning.DatasetCleaner;
import djl.handwritting.service.LettersDivider;
import djl.utils.CleaningArguments;

import java.nio.file.Paths;
import java.util.Arrays;

public class Cleaning {
    public static void main(String[] args) throws Exception {
        CleaningArguments arguments = CleaningArguments.parseArgs(args);

        LettersDivider lettersDivider = LettersDivider.builder()
                .imgWidth(arguments.getImageWidth())
                .imgHeight(arguments.getImageHeight())
                .colorThreshold(arguments.getColorThreshold())
                .build();

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
                .lettersDivider(lettersDivider)
                .build();

        cleanerTrain.cleanDataset();
    }
}