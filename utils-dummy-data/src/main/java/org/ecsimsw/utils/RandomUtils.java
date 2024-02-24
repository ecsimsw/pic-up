package org.giggles.utils;

import java.util.Random;

public class RandomUtils {

    private static final Random RANDOM = new Random();

    public static String alphabetAndNumber(int minLength, int maxLength) {
        var length = RANDOM.nextInt(maxLength - minLength + 1) + minLength;
        return RANDOM.ints(0, 61 + 1)
            .limit(length)
            .map(RandomUtils::randomAlphanumeric)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public static long number(long min, long max) {
        return RANDOM.nextLong(min, max + 1);
    }

    private static char randomAlphanumeric(int i) {
        // 48 ~ 57   -> 0 ~ 9    (number)
        // 65 ~ 90   -> 10 ~ 35  (Upper)
        // 97 ~ 122  -> 36 ~ 61  (Lower)
        if (i <= 9) {
            return (char) (i + 48);
        }
        if (i <= 35) {
            return (char) (i - 10 + 65);
        }
        return (char) (i - 36 + 97);
    }
}
