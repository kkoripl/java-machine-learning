package demo.utils;

import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import ai.djl.translate.TranslateException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CSVDataset extends RandomAccessDataset {

    private final List<CSVRecord> csvRecords;

    private CSVDataset(Builder builder) {
        super(builder);
        csvRecords = builder.csvRecords;
    }

    @Override
    public Record get(NDManager manager, long index) throws IOException, TranslateException {
        return null;
    }

    @Override
    protected long availableSize() {
        return 0;
    }

    public static final class Builder extends BaseBuilder<Builder> {
        List<CSVRecord> csvRecords;

        @Override
        protected Builder self() {
            return this;
        }

        CSVDataset build() throws IOException {
            String csvFilePath = "path/malicious_url_data.csv";
            try (Reader reader = Files.newBufferedReader(Paths.get(csvFilePath));
                 CSVParser csvParser =
                         new CSVParser(
                                 reader,
                                 CSVFormat.DEFAULT
                                         .withHeader("url", "isMalicious")
                                         .withFirstRecordAsHeader()
                                         .withIgnoreHeaderCase()
                                         .withTrim())) {
                csvRecords = csvParser.getRecords();
            }
            return new CSVDataset(this);
        }
    }

}

