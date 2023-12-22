package ecsimsw.picup.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Map;

import static ecsimsw.picup.config.DataSourceType.MASTER;
import static ecsimsw.picup.config.DataSourceType.SLAVE;

@Configuration
public class DataSourceConfig {

    public static final String DB_SOURCE_BEAN_ALIAS_MASTER = "MASTER_DB_SOURCE";
    public static final String DB_SOURCE_BEAN_ALIAS_SLAVE = "SLAVE_DB_SOURCE";

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
        routingDataSource.setTargetDataSources(Map.of(
            MASTER, masterDataSource,
            SLAVE, slaveDataSource
        ));
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

        // This DataSource proxy allows to avoid fetching JDBC Connections from a pool unless actually necessary.
        // JDBC transaction control can happen without fetching a Connection from the pool or communicating with the database;
        // this will be done lazily on first creation of a JDBC Statement. As a bonus, this allows for taking the transaction-synchronized read-only flag and/or isolation level into account in a routing DataSource (e.g. IsolationLevelDataSourceRouter).

        // Lazy using transaction
        // Reduce transaction period
        // Separate read source, write source
    }
}

class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        if (DataSourceTargetContextHolder.getTargetContext().isPresent()) {
            return DataSourceTargetContextHolder.getTargetContext().get();
        }

        var isReadOnlyQuery = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        System.out.println(isReadOnlyQuery);
        System.out.println(DataSourceStatusCache.isHealthy(SLAVE));
        if (isReadOnlyQuery && DataSourceStatusCache.isHealthy(SLAVE)) {
            return SLAVE;
        }
        return MASTER;
    }
}

