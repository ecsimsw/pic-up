package ecsimsw.picup.config;

import ecsimsw.picup.exception.DataSourceConnectionDownException;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static ecsimsw.picup.config.DataSourceType.MASTER;
import static ecsimsw.picup.config.DataSourceType.SLAVE;

public class DataSourceRoutingRule extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        if (DataSourceTargetContextHolder.getTargetContext().isPresent()) {
            return DataSourceTargetContextHolder.getTargetContext().get();
        }
        var isReadOnlyQuery = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (isReadOnlyQuery && !DataSourceHealth.isDown(SLAVE)) {
            return SLAVE;
        }
        if (!isReadOnlyQuery && DataSourceHealth.isDown(MASTER)) {
            throw new DataSourceConnectionDownException("Server can read only now");
        }
        return MASTER;
    }
}
