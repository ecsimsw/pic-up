package ecsimsw.picup.config;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ecsimsw.picup.config.DataSourceStatus.*;

public class DataSourceStatusCache {

    // TODO :: make private
    public static ConcurrentMap<DataSourceType, DataSourceStatus> statusMap = new ConcurrentHashMap<>();

    static {
        Arrays.stream(DataSourceType.values())
            .forEach(it -> statusMap.put(it, UNKNOWN));
    }

    public static void setHealthy(DataSourceType dataSource) {
        statusMap.put(dataSource, HEALTHY);
    }

    public static void setUnHealthy(DataSourceType dataSource) {
        statusMap.put(dataSource, UNHEALTHY);
    }

    public static void setUnKnown(DataSourceType dataSource) {
        statusMap.put(dataSource, UNKNOWN);
    }

    public static boolean isHealthy(DataSourceType dataSource) {
        return statusMap.getOrDefault(dataSource, UNKNOWN) == HEALTHY;
    }
}

enum DataSourceStatus {
    HEALTHY,
    UNHEALTHY,
    UNKNOWN
}
