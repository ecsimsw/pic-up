package ecsimsw.picup.config;

import static ecsimsw.picup.config.DBType.MASTER;
import static ecsimsw.picup.config.DBType.SLAVE;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Configuration
public class DataSourceConfig {

    private static final String DB_SOURCE_BEAN_ALIAS_MASTER = "MASTER_DB_SOURCE";
    private static final String DB_SOURCE_BEAN_ALIAS_SLAVE = "SLAVE_DB_SOURCE";

    @Bean(value = DB_SOURCE_BEAN_ALIAS_MASTER)
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(value = DB_SOURCE_BEAN_ALIAS_SLAVE)
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource(
        @Qualifier(DB_SOURCE_BEAN_ALIAS_MASTER) DataSource masterDataSource,
        @Qualifier(DB_SOURCE_BEAN_ALIAS_SLAVE) DataSource slaveDataSource
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
        var isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (isReadOnly) {
            return SLAVE;
        }
        return MASTER;
    }
}

enum DBType {
    MASTER,
    SLAVE
}