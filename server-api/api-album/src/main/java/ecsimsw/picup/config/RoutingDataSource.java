package ecsimsw.picup.config;

import ecsimsw.picup.exception.DataSourceConnectionDownException;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static ecsimsw.picup.config.DataSourceType.MASTER;
import static ecsimsw.picup.config.DataSourceType.SLAVE;

public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        if (DataSourceTargetContextHolder.getTargetContext().isPresent()) {
            return DataSourceTargetContextHolder.getTargetContext().get();
        }

        var isReadOnlyQuery = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (DataSourceHealth.isUp(MASTER) && DataSourceHealth.isUp(SLAVE)) {
            if (isReadOnlyQuery) {
                return SLAVE;
            }
            return MASTER;
        }

        if (isReadOnlyQuery && DataSourceHealth.isUp(SLAVE)) {
            return SLAVE;
        }

        if (!isReadOnlyQuery && DataSourceHealth.isUp(MASTER)) {
            throw new DataSourceConnectionDownException("Server is now read only");
        }

        if (DataSourceHealth.isUp(MASTER) && !DataSourceHealth.isUp(SLAVE)) {
            return MASTER;
        }
        throw new DataSourceConnectionDownException("DB server is down");
    }
}
