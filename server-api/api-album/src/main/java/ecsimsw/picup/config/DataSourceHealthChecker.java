package ecsimsw.picup.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceHealthChecker {

    @Qualifier(value = DataSourceConfig.DB_SOURCE_BEAN_ALIAS_MASTER)
    private final DataSource dataSourceMaster;

    @Qualifier(value = DataSourceConfig.DB_SOURCE_BEAN_ALIAS_SLAVE)
    private final DataSource dataSourceSlave;

    public DataSourceHealthChecker(DataSource dataSourceMaster, DataSource dataSourceSlave) {
        this.dataSourceMaster = dataSourceMaster;
        this.dataSourceSlave = dataSourceSlave;
    }

    @Scheduled(fixedDelay = 3000)
    public void test1() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceSlave);
        jdbcTemplate.query()
        final DataSourceHealthIndicator dataSourceHealthIndicator = new DataSourceHealthIndicator(dataSourceSlave);
        final Status status = dataSourceHealthIndicator.getHealth(false).getStatus();
        System.out.println(status);
        if(status == Status.UP) {
            DataSourceStatusCache.setHealthy(DataSourceType.SLAVE);
        }
        if(status == Status.DOWN) {
            DataSourceStatusCache.setUnHealthy(DataSourceType.SLAVE);
        }
    }
}
