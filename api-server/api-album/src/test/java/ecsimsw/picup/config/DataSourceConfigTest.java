package ecsimsw.picup.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@TestPropertySource(locations = "/application-dev.properties")
@SpringBootTest
class DataSourceConfigTest {

    private final RoutingDataSource routingDataSource;

    public DataSourceConfigTest(@Autowired RoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
    }

    @Transactional
    @DisplayName("Transactional readonly = false 인 경우 master db에 쿼리된다.")
    @Test
    void masterDataSource() {
        Object currentLookupKey = routingDataSource.determineCurrentLookupKey();
        assertEquals(DBType.MASTER, currentLookupKey);
    }

    @Transactional(readOnly = true)
    @DisplayName("Transactional readonly = true 인 경우 slave db에 쿼리된다.")
    @Test
    void slaveDataSource() {
        Object currentLookupKey = routingDataSource.determineCurrentLookupKey();
        assertEquals(DBType.SLAVE, currentLookupKey);
    }
}
