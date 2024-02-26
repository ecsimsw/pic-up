package org.ecsimsw.sample;

import java.util.List;
import org.ecsimsw.utils.CommaBasedRowFormat;
import org.ecsimsw.utils.RandomUtils;

public class AlbumRowFormat extends CommaBasedRowFormat {

    private final long userIdMin;
    private final long userIdMax;

    public AlbumRowFormat(long userIdMin, long userIdMax) {
        super(List.of("id", "userId", "title", "description"));
        this.userIdMin = userIdMin;
        this.userIdMax = userIdMax;
    }

    @Override
    public Iterable<? extends CharSequence> randomColumnValues(long id) {
        return List.of(
            String.valueOf(id),
            userId(),
            title(),
            description()
        );
    }

    public String userId() {
        return String.valueOf(RandomUtils.number(userIdMin, userIdMax));
    }

    public String title() {
        return RandomUtils.alphabetAndNumber(1, 10);
    }

    public String description() {
        return RandomUtils.alphabetAndNumber(1, 20);
    }
}
