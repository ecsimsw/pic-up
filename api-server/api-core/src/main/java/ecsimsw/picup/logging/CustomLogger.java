package ecsimsw.picup.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.logging.LogLevel;

public class CustomLogger {

    private final Marker marker;
    private final Logger logger;

    public CustomLogger(Marker marker, Logger logger) {
        this.marker = marker;
        this.logger = logger;
    }

    public CustomLogger(Logger logger) {
        this(null, logger);
    }

    public static CustomLogger init(String name) {
        return new CustomLogger(LoggerFactory.getLogger(name));
    }

    public static CustomLogger init(String markerName, Class<?> clazz) {
        return new CustomLogger(MarkerFactory.getMarker(markerName), LoggerFactory.getLogger(clazz));
    }

    public static CustomLogger init(Class<?> clazz) {
        return new CustomLogger(LoggerFactory.getLogger(clazz));
    }

    public void log(LogLevel logLevel, String message, Object... arguments) {
        if (logLevel == LogLevel.OFF) {
            return;
        }

        if (logLevel == LogLevel.TRACE) {
            trace(message, arguments);
            return;
        }

        if (logLevel == LogLevel.DEBUG) {
            debug(message, arguments);
            return;
        }

        if (logLevel == LogLevel.WARN) {
            warn(message, arguments);
            return;
        }

        if (logLevel == LogLevel.ERROR) {
            error(message, arguments);
            return;
        }

        info(message, arguments);
    }

    public void trace(String message, Object... arguments) {
        if(marker == null) {
            logger.trace(message, arguments);
            return;
        }
        logger.trace(marker, message, arguments);
    }

    public void debug(String message, Object... arguments) {
        if(marker == null) {
            logger.debug(message, arguments);
            return;
        }
        logger.debug(marker, message, arguments);
    }

    public void info(String message, Object... arguments) {
        if(marker == null) {
            logger.info(message, arguments);
            return;
        }
        logger.info(marker, message, arguments);
    }

    public void warn(String message, Object... arguments) {
        if(marker == null) {
            logger.warn(message, arguments);
            return;
        }
        logger.warn(marker, message, arguments);
    }

    public void error(String message, Object... arguments) {
        if(marker == null) {
            logger.error(message, arguments);
            return;
        }
        logger.error(marker, message, arguments);
    }
}
