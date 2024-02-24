package org.ecsimsw.sample;

import java.util.List;
import org.ecsimsw.utils.CommaBasedRowFormat;
import org.ecsimsw.utils.RandomUtils;

public class UserRowFormat extends CommaBasedRowFormat {

    public UserRowFormat() {
        super(List.of("id", "username", "password"));
    }

    @Override
    public Iterable<? extends CharSequence> randomColumnValues(long id) {
        return List.of(
            String.valueOf(id),
            username(id),
            password()
        );
    }

    public String username(long id) {
        return "user-" + id;
    }

    public String password() {
        return RandomUtils.alphabetAndNumber(1, 10);
    }
}
