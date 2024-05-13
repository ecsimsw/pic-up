package ecsimsw.picup.utils;

import ecsimsw.picup.DataCsvFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    private static final int UNIT_COUNT = 1_000_000;

    public static int generate(long idFrom, DataCsvFile dummy, int dataCount) {
        try (
            var bw = new BufferedWriter(new FileWriter(dummy.fileName(), true))
        ) {
            for (var i = 0; i < dataCount; i++) {
                bw.append(dummy.columnValueLine(",", idFrom++)).append("\n");
                log(idFrom);
            }
            return dataCount;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void log(long count) {
        if (count % UNIT_COUNT == 0) {
            System.out.println("append : " + count);
        }
    }
}
