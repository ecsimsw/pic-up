package ecsimsw.picup.config;

import ecsimsw.picup.album.exception.DataSourceConnectionDownException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DataSourceHealth {

    private static final ConcurrentMap<DataSourceType, Status> STATUS_MAP = new ConcurrentHashMap<>();

    static {
        Arrays.stream(DataSourceType.values())
            .forEach(it -> STATUS_MAP.put(it, Status.UNKNOWN));
    }

    private final DataSourceHealthIndicator indicatorMaster = new DataSourceHealthIndicator();
    private final DataSourceHealthIndicator indicatorSlave = new DataSourceHealthIndicator();

    public DataSourceHealth(
        @Qualifier(value = DataSourceConfig.DB_SOURCE_BEAN_ALIAS_MASTER)
        DataSource dataSourceMaster,
        @Qualifier(value = DataSourceConfig.DB_SOURCE_BEAN_ALIAS_SLAVE)
        DataSource dataSourceSlave
    ) {
        indicatorMaster.setDataSource(dataSourceMaster);
        indicatorSlave.setDataSource(dataSourceSlave);
    }

    @Scheduled(fixedDelay = 3000)
    public void healthCheck() {
        healthCheck(DataSourceType.MASTER, indicatorMaster);
        healthCheck(DataSourceType.SLAVE, indicatorSlave);
        alertDisaster();
        DataSourceTargetContextHolder.clearContext();
    }

    private void healthCheck(DataSourceType directTargetSource, DataSourceHealthIndicator indicator) {
        DataSourceTargetContextHolder.setContext(directTargetSource);
        var health = indicator.getHealth(false);
        STATUS_MAP.put(directTargetSource, health.getStatus());
    }

    private static void alertDisaster() {
        for (var sourceType : DataSourceType.values()) {
            var status = STATUS_MAP.get(sourceType);
            if (status != Status.UP) {
                throw new DataSourceConnectionDownException(sourceType + " is down, " + status);
            }
        }
    }

    public static boolean isDown(DataSourceType dataSourceType) {
        return STATUS_MAP.get(dataSourceType) == Status.DOWN;
    }
}
