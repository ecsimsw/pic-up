package ecsimsw.picup.dataUtils.strategy;

import java.util.Random;

//id,name,price,quantity

public class RandomProductCsvLine implements CsvLineStrategy {

    private static final Random RANDOM = new Random();
    private Long id = 0L;

    @Override
    public String retrieveNewLine() {
        id++;
        return String.join(",",
            String.valueOf(id),
            randomName(5, 15),
            String.valueOf(randomPrice(500, 100000)),
            String.valueOf(randomQuantity(1500))
        );
    }

    private int randomPrice(int min, int max) {
        int unitMoney = 1000;
        return (RANDOM.nextInt(max / unitMoney - min / unitMoney) + min / unitMoney) * unitMoney;
    }

    private int randomQuantity(int max) {
        return RANDOM.nextInt(max) + 1;
    }

    private String randomName(int minLength, int maxLength) {
        int length = RANDOM.nextInt(maxLength - minLength + 1) + minLength;
        return RANDOM.ints(0, 56 + 1)
            .limit(length)
            .map(it -> randomAlphanumeric(it))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    private char randomAlphanumeric(int i) {
        // 48 ~ 57   -> 0 ~ 9
        // 65 ~ 90   -> 10 ~ 35
        // 97 ~ 122  -> 36 ~ 61
        if (i <= 9) {
            return (char) (i + 48);
        }
        if (i <= 35) {
            return (char) (i - 10 + 65);
        }
        return (char) (i - 36 + 97);
    }
}
