package org.ecsimsw.sample;

import java.util.List;
import org.ecsimsw.utils.CommaBasedRowFormat;
import org.ecsimsw.utils.RandomUtils;

public class PictureRowFormat extends CommaBasedRowFormat {

    private final long albumIdMin;
    private final long albumIdMax;

    public PictureRowFormat(long albumIdMin, long albumIdMax) {
        super(List.of("id", "albumId", "description"));
        this.albumIdMin = albumIdMin;
        this.albumIdMax = albumIdMax;
    }

    @Override
    public Iterable<? extends CharSequence> randomColumnValues(long id) {
        return List.of(
            String.valueOf(id),
            albumId(),
            description()
        );
    }

    public String albumId() {
        return String.valueOf(RandomUtils.number(albumIdMin, albumIdMax));
    }

    public String description() {
        return RandomUtils.alphabetAndNumber(1, 20);
    }
}
