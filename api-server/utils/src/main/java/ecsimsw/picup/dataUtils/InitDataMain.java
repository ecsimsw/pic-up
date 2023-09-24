package ecsimsw.picup.utils;

import java.io.IOException;

public class InitDataMain {

    public static void main(String[] args) throws IOException {
        var csvLineStrategy = new RandomProductCsvLine();
        var initDataCSV = new InitDataCSV(csvLineStrategy, "init-data-1_000_000.csv", 1_000_000);
        initDataCSV.setInitialLine("id,name,price,quantity");
        initDataCSV.generate();
    }
}
