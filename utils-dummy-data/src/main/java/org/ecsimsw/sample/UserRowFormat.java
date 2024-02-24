package org.giggles.sample;

import org.giggles.utils.CommaBasedRowFormat;
import org.giggles.utils.RandomUtils;

import java.util.List;

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
        return RandomUtils.alphabetAndNumber(1, 10) + id;
    }

    public String password() {
        return RandomUtils.alphabetAndNumber(1, 10);
    }
}
