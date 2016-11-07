import de.ivz.plm.logging.UdpHandler;

import java.util.logging.*;
import java.util.logging.Logger;

/**
 * Configuration - Setting the IVZPLM logging configuration programmatically
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class Configuration {

    public final static String IVZPLM_HOST = "plm.host.address";
    public final static Integer IVZPLM_PORT = 6379;

    public Configuration() {
        // Console Logger
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);

        // NAGIOS Logger
        UdpHandler logHandler = new UdpHandler();
        logHandler.setLevel(Level.INFO);
        logHandler.setRemoteAddress(IVZPLM_HOST);
        logHandler.setRemotePort(IVZPLM_PORT);
        logHandler.setUserFields("type:log");
        Logger logLogger = Logger.getLogger("IVZPLM-LOG");
        logLogger.setUseParentHandlers(true);
        logLogger.addHandler(logHandler);

        // NAGIOS Logger
        UdpHandler nagiosHandler = new UdpHandler();
        nagiosHandler.setLevel(Level.INFO);
        nagiosHandler.setRemoteAddress(IVZPLM_HOST);
        nagiosHandler.setRemotePort(IVZPLM_PORT);
        nagiosHandler.setUserFields("type:nagios,host:ivzsmbox.ivz.de");
        Logger nagiosLogger = Logger.getLogger("IVZPLM-NAGIOS");
        nagiosLogger.setUseParentHandlers(false);
        nagiosLogger.addHandler(nagiosHandler);

        // SYSTEM Logger
        UdpHandler systemHandler = new UdpHandler();
        systemHandler.setLevel(Level.INFO);
        systemHandler.setRemoteAddress(IVZPLM_HOST);
        systemHandler.setRemotePort(IVZPLM_PORT);
        systemHandler.setUserFields("type:system");
        Logger systemLogger = Logger.getLogger("IVZPLM-SYSTEM");
        systemLogger.setUseParentHandlers(false);
        systemLogger.addHandler(systemHandler);

        // JDBC Logger
        UdpHandler jdbcHandler = new UdpHandler();
        jdbcHandler.setLevel(Level.INFO);
        jdbcHandler.setRemoteAddress(IVZPLM_HOST);
        jdbcHandler.setRemotePort(IVZPLM_PORT);
        jdbcHandler.setUserFields("type:jdbc");
        Logger jdbcLogger = Logger.getLogger("IVZPLM-JDBC");
        jdbcLogger.setUseParentHandlers(false);
        jdbcLogger.addHandler(jdbcHandler);

        // Root Logger
        Logger.getLogger("").addHandler(consoleHandler);
        Logger.getLogger("").addHandler(logHandler);
    }

    public static void main(String[] argv) {
        new Configuration();
    }
}
