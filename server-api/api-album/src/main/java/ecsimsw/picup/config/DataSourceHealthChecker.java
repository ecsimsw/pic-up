package ecsimsw.picup.config;

import ecsimsw.picup.exception.DataSourceConnectionDownException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ecsimsw.picup.config.DataSourceType.MASTER;
import static ecsimsw.picup.config.DataSourceType.SLAVE;

@Component
public class DataSourceHealthChecker {

    public static final ConcurrentMap<DataSourceType, Status> STATUS_MAP = new ConcurrentHashMap<>();

    static {
        Arrays.stream(DataSourceType.values())
            .forEach(it -> STATUS_MAP.put(it, Status.UNKNOWN));
    }

    private final DataSourceHealthIndicator indicatorMaster = new DataSourceHealthIndicator();
    private final DataSourceHealthIndicator indicatorSlave = new DataSourceHealthIndicator();

    public DataSourceHealthChecker(
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
        var healthMaster = indicatorMaster.getHealth(false);
        if (healthMaster.getStatus() == Status.DOWN) {
            throw new DataSourceConnectionDownException(MASTER + " is down, " + healthMaster.getDetails());
        }
        STATUS_MAP.put(MASTER, healthMaster.getStatus());

        var healthSlave = indicatorSlave.getHealth(false);
        if (healthSlave.getStatus() == Status.DOWN) {
            throw new DataSourceConnectionDownException(SLAVE + " is down, " + healthSlave.getDetails());
        }
        STATUS_MAP.put(SLAVE, healthSlave.getStatus());

        DataSourceTargetContextHolder.clearContext();
    }
}
