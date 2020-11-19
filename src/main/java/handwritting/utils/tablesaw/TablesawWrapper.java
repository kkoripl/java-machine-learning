package handwritting.utils.tablesaw;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static tech.tablesaw.api.ColumnType.STRING;

public class TablesawWrapper {

    public static void cleanCsvFile(String csvFilePath, String labelColumn, List<String> unwantedLabels, List<String> unwantedContain) throws Exception {
        Table csvData = readCsvFile(csvFilePath);
        csvData = removeUnwantedValues(csvData, labelColumn, unwantedLabels);
        csvData = removeWhereContainsInColumn(csvData, labelColumn, unwantedContain);

        upperCaseColumn(csvData, labelColumn);
        exportCsv(csvData, csvFilePath);
    }

    public static Table readCsvFile(String filePath) throws IOException {
        CsvReadOptions.Builder b = CsvReadOptions.builder(filePath)
                .columnTypes(new ColumnType[]{STRING, STRING})
                .separator(',')
                .header(true);
        return Table.read().csv(b);
    }

    public static void exportCsv(Table table, String csvFilePath) throws IOException {
        table.write().csv(csvFilePath);
    }

    public static Set<Character> getUniqueLetters(Table table, String column) {
        return getUniqueValues(table, column).stream()
                .flatMapToInt(CharSequence::chars)
                .mapToObj(letter -> ((char) letter))
                .filter(letter -> !letter.equals(' '))
                .collect(Collectors.toSet());
    }

    public static List<String> getColumnForUnwantedValuesInOtherCol(String csvFilePath, String wantedColumn, String unwantedColumn, List<String> unwantedValues) throws IOException {
        Table table = readCsvFile(csvFilePath);
        return getColumnForUnwantedValuesInOtherCol(table, wantedColumn, unwantedColumn, unwantedValues);
    }

    public static List<String> getColumnForUnwantedContainInOtherCol(String csvFilePath, String wantedColumn, String unwantedColumn, List<String> values) throws IOException {
        Table table = readCsvFile(csvFilePath);
        List<String> columns = new ArrayList<>();
        for (String value : values) {
            columns.addAll(table.stringColumn(wantedColumn).where(table.stringColumn(unwantedColumn).containsString(value)).asList());
        }
        return columns;
    }

    private static List<String> getColumnForUnwantedValuesInOtherCol(Table table, String wantedColumn, String unwantedColumn, List<String> unwantedValues) {
        return table.stringColumn(wantedColumn).where(table.stringColumn(unwantedColumn).isIn(unwantedValues)).asList();
    }

    public static Table removeUnwantedValues(Table table, String column, List<String> values) {
        for (String value : values) {
            table = removeWhereValueInColumn(table, column, value);
        }
        return table;
    }

    public static Table removeWhereValueInColumn(Table table, String column, String value) {
        return table.dropWhere(table.stringColumn(column).isEqualTo(value));
    }

    public static Table removeWhereContainsInColumn(Table table, String column, List<String> values) {
        for (String value : values) {
            table = removeWhereContainsInColumn(table, column, value);
        }
        return table;
    }

    public static Table removeWhereContainsInColumn(Table table, String column, String value) {
        return table.dropWhere(table.stringColumn(column).containsString(value));
    }

    public static void upperCaseColumn(Table table, String column) {
        StringColumn upperCase = table.stringColumn(column).upperCase();
        upperCase.setName(column);
        table.removeColumns(column);
        table.addColumns(upperCase);
    }

    public static List<String> getUniqueValues(Table table, String column) {
        return table.stringColumn(column).unique().asList();
    }

    public static int size(Table table) {
        return table.rowCount();
    }

    public static String getStringValueByIndex(Table table, String column, int index) {
        return table.getString(index, column);
    }
}