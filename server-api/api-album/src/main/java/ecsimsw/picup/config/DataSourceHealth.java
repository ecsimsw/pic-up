package ecsimsw.picup.config;

import static ecsimsw.picup.config.DataSourceType.MASTER;
import static ecsimsw.picup.config.DataSourceType.SLAVE;

import ecsimsw.picup.album.exception.DataSourceConnectionDownException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataSourceHealth {

    private static final ConcurrentMap<DataSourceType, Status> STATUS_MAP = new ConcurrentHashMap<>(Map.of(
        MASTER, Status.UNKNOWN,
        SLAVE, Status.UNKNOWN
    ));

    private final DataSourceHealthIndicator indicatorMaster;
    private final DataSourceHealthIndicator indicatorSlave;

    public DataSourceHealth(
        @Qualifier(value = DataSourceConfig.DB_SOURCE_BEAN_ALIAS_MASTER)
        DataSource dataSourceMaster,
        @Qualifier(value = DataSourceConfig.DB_SOURCE_BEAN_ALIAS_SLAVE)
        DataSource dataSourceSlave
    ) {
        indicatorMaster = new DataSourceHealthIndicator(dataSourceMaster);
        indicatorSlave = new DataSourceHealthIndicator(dataSourceSlave);
    }

    @Scheduled(fixedDelay = 30000)
    public void healthCheck() {
        DataSourceTargetContextHolder.setContext(MASTER);
        STATUS_MAP.put(MASTER, indicatorMaster.getHealth(false).getStatus());

        DataSourceTargetContextHolder.setContext(SLAVE);
        STATUS_MAP.put(SLAVE, indicatorSlave.getHealth(false).getStatus());

        STATUS_MAP.keySet().stream()
            .filter(key -> STATUS_MAP.get(key) == Status.DOWN)
            .forEach(key -> {
                throw new DataSourceConnectionDownException(key + " is down");
            });
        DataSourceTargetContextHolder.clearContext();
    }

    public static boolean isDown(DataSourceType dataSourceType) {
        return STATUS_MAP.get(dataSourceType) == Status.DOWN;
    }
}
