package rocks.spaghetti.tedium.renderer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private Log() {
        throw new IllegalStateException("Utility Class");
    }

    private static Logger LOGGER = LogManager.getLogger("tedium-renderer");

    public static void info(String msg, Object... args) {
        LOGGER.log(Level.INFO, msg, args);
    }
}
