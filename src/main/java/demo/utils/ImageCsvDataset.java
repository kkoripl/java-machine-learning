package demo.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import djl.utils.tablesaw.TablesawWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tech.tablesaw.api.Table;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ImageCsvDataset extends RandomAccessDataset {

    private final Table records;
    private final String imageNameColumn;
    private final String labelColumn;
    private final String imageDatasetPath;
    
    private final TablesawWrapper tablesawWrapper = new TablesawWrapper();

    private ImageCsvDataset(Builder builder) {
        super(builder);
        this.records = builder.getRecords();
        this.imageNameColumn = builder.getImageNameColumn();
        this.labelColumn = builder.getLabelColumn();
        this.imageDatasetPath = builder.getImageDatasetPath();
    }

    @Override
    public Record get(NDManager manager, long index) throws IOException {
        NDArray datum = ImageFactory.getInstance()
                .fromFile(generateImagePath(imageDatasetPath, imageNameColumn, index))
                .toNDArray(manager, Image.Flag.GRAYSCALE);
        NDArray label = manager.create(getLabel(labelColumn, index).getBytes());

        return new Record(new NDList(datum), new NDList(label));
    }

    @Override
    protected long availableSize() {
        return tablesawWrapper.size(records);
    }

    protected List<String> getSynset() {
        return tablesawWrapper.getUniqueValues(records, labelColumn);
    }

    public static Builder builder(TablesawWrapper tablesawWrapper) {
        return new Builder(tablesawWrapper);
    }

    private Path generateImagePath(String datasetPath, String imgCol, long imageIdx) {
        String imgName = tablesawWrapper.getStringValueByIndex(records, imgCol, Math.toIntExact(imageIdx));
        return Paths.get(datasetPath, imgName);
    }

    private String getLabel(String labCol, long idx) {
        return tablesawWrapper.getStringValueByIndex(records, labCol, Math.toIntExact(idx));
    }

    @Getter
    public static final class Builder extends BaseBuilder<Builder> {

        private String csvPath;
        private String imageDatasetPath;
        private String imageNameColumn;
        private String labelColumn;
        private Table records;
        private final TablesawWrapper tablesawWrapper;

        public Builder(TablesawWrapper tablesawWrapper){
            this.tablesawWrapper = tablesawWrapper;
        }

        @Override
        protected Builder self() {
            return this;
        }

        public ImageCsvDataset build() throws IOException {
            records = TablesawWrapper.readCsvFile(csvPath);
            return new ImageCsvDataset(this);
        }

        public Builder setCsvPath(String csvPath) {
            this.csvPath = csvPath;
            return self();
        }

        public Builder setImageDatasetPath(String imageDatasetPath) {
            this.imageDatasetPath = imageDatasetPath;
            return self();
        }

        public Builder setImageNameColumn(String imageNameColumn) {
            this.imageNameColumn = imageNameColumn;
            return self();
        }

        public Builder setLabelColumn(String labelColumn) {
            this.labelColumn = labelColumn;
            return self();
        }
    }
}
