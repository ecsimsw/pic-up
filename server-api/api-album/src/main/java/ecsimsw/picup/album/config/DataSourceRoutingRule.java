package ecsimsw.picup.album.config;

import ecsimsw.picup.album.exception.DataSourceConnectionDownException;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DataSourceRoutingRule extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        if (DataSourceTargetContextHolder.getTargetContext().isPresent()) {
            return DataSourceTargetContextHolder.getTargetContext().get();
        }
        var isReadOnlyQuery = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (isReadOnlyQuery && !DataSourceHealth.isDown(DataSourceType.SLAVE)) {
            return DataSourceType.SLAVE;
        }
        if (!isReadOnlyQuery && DataSourceHealth.isDown(DataSourceType.MASTER)) {
            throw new DataSourceConnectionDownException("Server can read only now");
        }
        return DataSourceType.MASTER;
    }
}
