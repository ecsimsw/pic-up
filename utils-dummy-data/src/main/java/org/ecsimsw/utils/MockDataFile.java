package org.giggles.utils;

import org.giggles.utils.DataFileInfo;
import org.giggles.utils.RowFormatStrategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MockDataFile {

    private static final int UNIT_COUNT = 1_000_000;

    public static DataFileInfo generate(String fileName, RowFormatStrategy rowFormatStrategy, int dataCount) throws IOException {
        var startTime = System.currentTimeMillis();
        appendAsFile(fileName, rowFormatStrategy, dataCount);
        var endTime = System.currentTimeMillis();
        return new DataFileInfo(fileName, fileSize(fileName), dataCount, endTime - startTime);
    }

    private static void appendAsFile(String fileName, RowFormatStrategy rowFormatStrategy, int dataCount) throws IOException {
        try (
            var bw = new BufferedWriter(new FileWriter(fileName, false))
        ) {
            bw.append(rowFormatStrategy.columnNameLine());

            var id = 1;
            for (var i = 0; i < dataCount / UNIT_COUNT; i++) {
                for (var j = 0; j < UNIT_COUNT; j++) {
                    bw.append("\n").append(rowFormatStrategy.row(id++));
                }
                System.out.println("append : " + (i + 1) * UNIT_COUNT);
            }
            for (var i = 0; i < dataCount % UNIT_COUNT; i++) {
                bw.append("\n").append(rowFormatStrategy.row(id++));
            }
            System.out.println("append : " + dataCount);
        }
    }

    private static long fileSize(String fileName) throws IOException {
        return Files.size(Paths.get(fileName));
    }
}
