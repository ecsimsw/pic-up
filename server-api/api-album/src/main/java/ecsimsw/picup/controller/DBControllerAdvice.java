package ecsimsw.picup.controller;

import ecsimsw.picup.config.DataSourceStatusCache;
import ecsimsw.picup.config.DataSourceType;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DBControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBControllerAdvice.class);

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<String> dbDown() {
//        DataSourceStatusCache.setUnHealthy(DataSourceType.SLAVE);
        return ResponseEntity.status(500).body("server down");
    }
}
