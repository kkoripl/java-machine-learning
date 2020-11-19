package handwritting.preparators;

import handwritting.utils.opencv.OpenCVWrapper;
import handwritting.utils.opencv.OpenCvColor;
import handwritting.utils.tablesaw.TablesawWrapper;
import lombok.Builder;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Builder
public class DatasetCleaner {

    private String csvDatasetPath;
    private String imageDatasetPath;
    private String lettersDatasetPath;
    private String imageColumn;
    private String labelColumn;
    private List<String> unwantedLabels;
    private List<String> unwantedSigns;
    private double unwantedSimilarityThreshold;
    private String unwantedPartPath;

    public void showSetSummary() throws IOException {
        Table csvData = TablesawWrapper.readCsvFile(csvDatasetPath);

        System.out.println(csvData.shape());
        System.out.println("-------------------------- ");

        System.out.println(csvData.structure().printAll());
        System.out.println("-------------------------- ");

        System.out.println(csvData.first(3));
        System.out.println("-------------------------- ");

        StringColumn labels = csvData.stringColumn("IDENTITY");
        labels.sortAscending();
        System.out.println(labels.unique().print());
        System.out.println("-------------------------- ");

        Table csvDataLabels = csvData.countBy(labelColumn).sortDescendingOn("Count");
        System.out.println(csvDataLabels.print());
        System.out.println("-------------------------- ");

        System.out.println(csvDataLabels.where(csvDataLabels.stringColumn("Category").isIn("EMPTY", "UNREADABLE", "")));
        System.out.println("-------------------------- ");
    }

    public void cleanDataset() throws Exception {
        makeToSmallUnreadable(csvDatasetPath, imageDatasetPath);

        List<String> imgsToDelete = TablesawWrapper.getColumnForUnwantedValuesInOtherCol(csvDatasetPath, imageColumn, labelColumn, unwantedLabels);
        List<String> imgsToDelete2 = TablesawWrapper.getColumnForUnwantedContainInOtherCol(csvDatasetPath, imageColumn, labelColumn, unwantedSigns);

        deleteDatasetImages(imageDatasetPath, imgsToDelete);
        deleteDatasetImages(imageDatasetPath, imgsToDelete2);

        cleanImages(imageDatasetPath, unwantedPartPath, unwantedSimilarityThreshold);
        resizeDatasetToBiggestDimension(OpenCvColor.WHITE.getScalar());
        TablesawWrapper.cleanCsvFile(csvDatasetPath, labelColumn, unwantedLabels, unwantedSigns);
        buildLetterDirectories(lettersDatasetPath, csvDatasetPath, labelColumn);
        divideIntoLetters(csvDatasetPath, imageDatasetPath, imageColumn, labelColumn, lettersDatasetPath);
    }

    private void resizeDatasetToBiggestDimension(Scalar fillWithColor) throws IOException {
        Size biggestDimension = findBiggestDimension();
        OpenCVWrapper.resizeImagesAndFillWithColor(imageDatasetPath, biggestDimension, fillWithColor);
    }

    public Size findBiggestDimension() throws IOException {
        return OpenCVWrapper.findBiggestDimension(imageDatasetPath);
    }

    private void deleteDatasetImages(String datasetPath, List<String> imgNames) {
        imgNames.forEach(img -> {
            try {
                Files.deleteIfExists(Paths.get(datasetPath, img));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void cleanImages(String imageDatasetPath, String notWantedPartPath, double similarityThreshold) throws IOException {
        OpenCVWrapper.cleanImages(imageDatasetPath, notWantedPartPath, similarityThreshold);
    }

    private void buildLetterDirectories(String rootPath, String csvDatasetPath, String column) throws IOException {
        Table table = TablesawWrapper.readCsvFile(csvDatasetPath);
        Set<Character> letters = TablesawWrapper.getUniqueLetters(table, column);

        createDirIfNotExist(rootPath);

        for (Character letter : letters) {
            Path letterPath = Paths.get(rootPath, letter.toString());
            createDirIfNotExist(letterPath.toString());
        }
    }

    private void createDirIfNotExist(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void makeToSmallUnreadable(String csvDatasetPath, String imageDatasetPath) throws IOException {
        Table table = TablesawWrapper.readCsvFile(csvDatasetPath);
        List<Integer> toSmallIdxs = new ArrayList<>();
        Mat template = OpenCVWrapper.loadImage(unwantedPartPath);

        for (int idx = 0; idx < table.rowCount(); idx++) {
            Mat img = OpenCVWrapper.loadImage(Paths.get(imageDatasetPath, table.getString(idx, imageColumn)).toString());
            if (img.size().height <= template.size().height ||
                img.size().width <= template.size().width) {
                toSmallIdxs.add(idx);
            }
        }

        table.stringColumn(labelColumn).set(Selection.with(toSmallIdxs.stream().mapToInt(i->i).toArray()), "UNREADABLE");
        TablesawWrapper.exportCsv(table, csvDatasetPath);
    }

    private void divideIntoLetters(String csvDatasetPath, String imageDatasetPath, String imageColumn, String labelColumn, String rootPath) throws IOException {
        Table table = TablesawWrapper.readCsvFile(csvDatasetPath);
        int wellDivided = 0;
        int letterCnt = 0;
        List<String> wronglyDividedLabels = new ArrayList<>();
        List<Integer> wronglyDividedIdx = new ArrayList<>();
        System.out.println("Dividing");

        for (int rowIdx = 0; rowIdx < table.rowCount(); rowIdx++) {
            String label = table.row(rowIdx).getString(labelColumn).replace(" ","");
            String image = table.row(rowIdx).getString(imageColumn);

            if (rowIdx % 999 == 0) {
                System.out.println("Dividing: " + (rowIdx+1));
            }

            List<Mat> letters = LettersDivider.findLetters(OpenCVWrapper.loadImage(Paths.get(imageDatasetPath, image).toString()));
            if (letters.size() == label.length()) {
                wellDivided++;
                for (int i=0; i<letters.size(); i++) {
                    letterCnt++;
                    OpenCVWrapper.saveImage(letters.get(i), Paths.get(rootPath, ((Character) label.charAt(i)).toString(), String.valueOf(letterCnt)).toString()+".jpg");
                }
            } else {
                wronglyDividedLabels.add(label);
                wronglyDividedIdx.add(rowIdx);
            }
        }

        System.out.println("Well divided: " + wellDivided + " / " + table.rowCount());
        System.out.println("Found letters: " + letterCnt);
        System.out.println("koniec");

        table.dropRows(wronglyDividedIdx.stream().mapToInt(i->i).toArray());
        TablesawWrapper.exportCsv(table, csvDatasetPath);
    }
}