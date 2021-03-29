package rocks.spaghetti.tedium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private Log() { throw new IllegalStateException("Utility Class"); }

    private static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

    public static void trace(String message, Object... params) {
        LOGGER.trace(message, params);
    }

    public static void debug(String message, Object... params) {
        LOGGER.debug(message, params);
    }

    public static void info(String message, Object... params) {
        LOGGER.info(message, params);
    }

    public static void warn(String message, Object... params) {
        LOGGER.warn(message, params);
    }

    public static void error(String message, Object... params) {
        LOGGER.error(message, params);
    }

    public static void fatal(String message, Object... params) {
        LOGGER.fatal(message, params);
    }

    public static void catching(Throwable throwable) {
        LOGGER.catching(throwable);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
