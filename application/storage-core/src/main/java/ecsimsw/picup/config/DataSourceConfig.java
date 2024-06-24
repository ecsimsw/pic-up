package ecsimsw.picup.config;

import static ecsimsw.picup.config.DataSourceType.MASTER;
import static ecsimsw.picup.config.DataSourceType.SLAVE;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Configuration
public class DataSourceConfig {

    private static final String MASTER_BEAN_NAME = "MASTER";
    private static final String SLAVE_BEAN_NAME = "SLAVE";

    @Bean(value = MASTER_BEAN_NAME)
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(value = SLAVE_BEAN_NAME)
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource(
        @Qualifier(MASTER_BEAN_NAME) DataSource masterDataSource,
        @Qualifier(SLAVE_BEAN_NAME) DataSource slaveDataSource
    ) {
        var routingDataSource = new RoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.setTargetDataSources(
            Map.of(
                MASTER, masterDataSource,
                SLAVE, slaveDataSource
            )
        );
        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        var determinedDataSource = routingDataSource(
            masterDataSource(),
            slaveDataSource()
        );
        return new LazyConnectionDataSourceProxy(determinedDataSource);
    }
}

class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        if(!TransactionSynchronizationManager.isSynchronizationActive()) {
            return MASTER;
        }
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            return SLAVE;
        }
        return MASTER;
    }
}
