package ecsimsw.picup.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;

//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Configuration
public class DataSourceConfig {

    @Bean(value = "MASTER")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(value = "SLAVE")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource2(
        @Qualifier(value = "MASTER") DataSource masterDataSource, // (1)
        @Qualifier(value = "SLAVE") DataSource slaveDataSource
    ) {
        RoutingDataSource routingDataSource = new RoutingDataSource(); // (2)

        HashMap<Object, Object> dataSourceMap = new HashMap<>(); // (3)
        dataSourceMap.put("master", masterDataSource);
        dataSourceMap.put("slave", slaveDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap); // (4)
        routingDataSource.setDefaultTargetDataSource(masterDataSource); // (5)

        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource determinedDataSource = routingDataSource2(
            masterDataSource(),
            slaveDataSource()
        );
        return new LazyConnectionDataSourceProxy(determinedDataSource);
    }

}
