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
        DataSourceTargetContextHolder.setContext(DataSourceType.MASTER);
        var healthMaster = indicatorMaster.getHealth(false);
        if (healthMaster.getStatus() != Status.UP) {
            throw new DataSourceConnectionDownException(DataSourceType.MASTER + " is down, " + healthMaster.getStatus());
        }
        STATUS_MAP.put(DataSourceType.MASTER, healthMaster.getStatus());

        DataSourceTargetContextHolder.setContext(DataSourceType.SLAVE);
        var healthSlave = indicatorSlave.getHealth(false);
        if (healthSlave.getStatus() != Status.UP) {
            throw new DataSourceConnectionDownException(DataSourceType.SLAVE + " is down, " + healthSlave.getStatus());
        }
        STATUS_MAP.put(DataSourceType.SLAVE, healthSlave.getStatus());

        DataSourceTargetContextHolder.clearContext();
    }

    public static boolean isDown(DataSourceType dataSourceType) {
        return STATUS_MAP.get(dataSourceType) == Status.DOWN;
    }

    public static boolean isUp(DataSourceType dataSourceType) {
        return STATUS_MAP.get(dataSourceType) == Status.UP;
    }
}
