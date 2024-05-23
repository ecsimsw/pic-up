package ecsimsw.picup.profile;

import org.springframework.core.env.AbstractEnvironment;

import java.util.stream.Stream;

public class ProfileUtils {

    public static String[] profilesFromModules(String... moduleNames) {
        var env = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        return Stream.of(moduleNames)
            .map(module -> module + "-" + env)
            .toArray(String[]::new);
    }
}
