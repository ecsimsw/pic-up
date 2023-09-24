package ecsimsw.mymarket.dataUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*
Generate init data as csv file.

sample data : 10_00_000
file size : 20MB
execution time : 1s

sample data : 100_000_000
file size : 2GB
execution time : 100s
 */

public class InitDataCSV {

    private static final int UNIT_COUNT = 1_000_000;

    private final CsvLineStrategy csvLineStrategy;
    private final String fileName;
    private final int dataCount;
    private String initialLine;

    public InitDataCSV(CsvLineStrategy csvLineStrategy, String fileName, int dataCount) {
        this.csvLineStrategy = csvLineStrategy;
        this.fileName = fileName;
        this.dataCount = dataCount;
    }

    public void generate() throws IOException {
        generate(false);
    }

    public void generate(boolean appendEnable) throws IOException {
        final long startTime = System.currentTimeMillis();
        try (
            final var bw = new BufferedWriter(new FileWriter(fileName, appendEnable))
        ) {
            if(initialLine != null) {
                bw.append(initialLine);
            }
            for (int i = 0; i < dataCount / UNIT_COUNT; i++) {
                for (int j = 0; j < UNIT_COUNT; j++) {
                    bw.append("\n").append(csvLineStrategy.retrieveNewLine());
                }
            }
            for (int i = 0; i < dataCount % UNIT_COUNT; i++) {
                bw.append("\n").append(csvLineStrategy.retrieveNewLine());
            }
        }
        final long finishTime = System.currentTimeMillis();
        System.out.println("Total generated rows : " + dataCount);
        System.out.println("Execution Time : " + (finishTime - startTime)/1000 + " sec");
    }

    public void setInitialLine(String line) {
        if(line != null) {
            this.initialLine = line;
        }
    }
}
