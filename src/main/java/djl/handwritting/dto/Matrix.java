package djl.handwritting.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Matrix {
    int[][] data;
    int rows, columns;

    public Matrix(int rows, int columns) {
        data = new int[rows][columns];
        this.rows = rows;
        this.columns = columns;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                data[y][x] = 0;
            }
        }
    }

    public Matrix(int[][] data, int rows, int columns) {
        this.data = data;
        this.rows = rows;
        this.columns = columns;
    }

    public void setValue(int row, int column, int value) {
        this.data[row][column] = value;
    }

    public int getValue(int row, int column) {
        return this.data[row][column];
    }

    public void add(int scaler) {
        for(int y = 0; y < rows; y++) {
            for(int x = 0; x < columns; x++) {
                this.data[y][x] += scaler;
            }
        }
    }

    public void add(Matrix m) {
        if(columns != m.columns || rows != m.rows) {
            System.out.println("Shape Mismatch");
            return;
        }

        for(int y = 0; y < rows; y++) {
            for(int x = 0; x < columns; x++) {
                this.data[y][x] += m.data[y][x];
            }
        }
    }

    public void multiply(Matrix a) {
        for(int y = 0; y < a.rows; y++) {
            for(int x = 0 ;x < a.columns; x++) {
                this.data[y][x] *= a.data[y][x];
            }
        }
    }

    public void multiply(int a) {
        for(int y = 0; y < rows; y++){
            for(int x = 0; x < columns; x++) {
                this.data[y][x] *= a;
            }
        }
    }

    public boolean contains(int value) {
        for(int y = 0; y < rows; y++){
            for(int x = 0; x < columns; x++) {
                if (this.data[y][x] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    public Matrix binarizeOnValue(int value) {
        Matrix binarized = new Matrix(this.rows, this.columns);
        for(int y = 0; y < rows; y++){
            for(int x = 0; x < columns; x++) {
                if (this.data[y][x] == value) {
                    binarized.setValue(y, x, 1);
                } else {
                    binarized.setValue(y, x, 0);
                }
            }
        }
        return binarized;
    }

    public void invertValues() {
        for(int y = 0; y < rows; y++){
            for(int x = 0; x < columns; x++) {
                if (this.data[y][x] == 1) {
                    this.setValue(y, x, 0);
                } else {
                    this.setValue(y, x, 1);
                }
            }
        }
    }

    public List<Integer> uniqueValues() {
        List<Integer> list = new ArrayList<>();
        for (int[] arr : this.data) {
            for (int val : arr) {
                list.add(val);
            }
        }
        return new ArrayList<>(new HashSet<>(list));
    }

    public int[] asArray() {
        int[] arr = new int[this.rows * this.columns];
        int idx = 0;
        for(int y = 0; y < rows; y++){
            for(int x = 0; x < columns; x++) {
                arr[idx] = this.data[y][x];
                idx += 1;
            }
        }

        return arr;
    }

    public int[][] copyData() {
        int[][] result = new int[this.getRows()][this.getColumns()];
        for (int y=0; y<this.getRows(); y++) {
            result[y] = data[y].clone();
        }
        return result;
    }

    public void cropByValue(int value) {
        int leftIdx = firstLeftIdxOfValue(value);
        int rightIdx = firstRightIdxOfValue(value);
        int upperIdx = firstUpperIdxOfValue(value);
        int lowerIdx = firstLowerIdxOfValue(value);

        int[][] newData = new int[lowerIdx-upperIdx+1][rightIdx-leftIdx+1];
        for (int y=0; y<lowerIdx-upperIdx+1; y++) {
            for (int x=0; x<rightIdx-leftIdx+1; x++) {
                newData[y][x] = data[upperIdx+y][leftIdx+x];
            }
        }

        setData(newData);
        setColumns(rightIdx-leftIdx+1);
        setRows(lowerIdx-upperIdx+1);
    }

    public int firstLeftIdxOfValue(int value) {
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                if (data[y][x] == value) {
                    return x;
                }
            }
        }
        return -1;
    }

    public int firstRightIdxOfValue(int value) {
        for (int x = columns-1; x >= 0 ; x--) {
            for (int y = rows-1; y >= 0; y--) {
                if (data[y][x] == value) {
                    return x;
                }
            }
        }
        return columns;
    }

    private int firstUpperIdxOfValue(int value) {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (data[y][x] == value) {
                    return y;
                }
            }
        }
        return -1;
    }

    private int firstLowerIdxOfValue(int value) {
        for (int y = rows-1; y >= 0; y--) {
            for (int x = columns-1; x >= 0; x--) {
                if (data[y][x] == value) {
                    return y;
                }
            }
        }
        return rows;
    }

    public void print(){
        System.out.println(get2DArrayPrint());
    }

    private String get2DArrayPrint() {
        String output = new String();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                output = output + data[y][x] + "\t";
            }
            output = output + "\n";
        }
        return output;
    }
}
