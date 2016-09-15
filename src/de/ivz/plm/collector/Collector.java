package de.ivz.plm.collector;

import de.ivz.plm.util.option.MultiOptionParser;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collector - Ein abstrakter Kollector, welcher um die spezifische Implementierung erweitert werden muss
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public abstract class Collector extends TimerTask {

    protected static final Logger log = Logger.getLogger(Collector.class.getName());

    public static final String OPTION_CONFIG           = "config";
    public static final String OPTION_DELAY            = "delay";
    public static final Long   OPTION_DELAY_DEFAULT    = 10l;
    public static final String OPTION_INTERVAL         = "interval";
    public static final Long   OPTION_INTERVAL_DEFAULT = 60l;
    public static final String OPTION_LOGGER           = "logger";

    protected MultiOptionParser optionParser;
    protected Long delay;
    protected Long interval;
    protected String logger;

    /**
     * Konstruktor mit Angabe eines Arrays von Optionen aus z.B. <I>public static void main(String[] args)</I>
     * @param args String-Array mit den Optionen
     */
    protected Collector(String[] args) {
        log.log(Level.FINE, "initialing");
        try {
            optionParser = new MultiOptionParser(args, OPTION_CONFIG);
            delay = optionParser.getLongOption(OPTION_DELAY, OPTION_DELAY_DEFAULT);
            log.log(Level.INFO, OPTION_DELAY + "=" + delay);
            interval = optionParser.getLongOption(OPTION_INTERVAL, OPTION_INTERVAL_DEFAULT);
            log.log(Level.INFO, OPTION_INTERVAL + "=" + interval);
            logger = optionParser.getStringOption(OPTION_LOGGER, null);
            log.log(Level.INFO, OPTION_LOGGER + "=" + logger);
        } catch (Exception e) {
            log.log(Level.SEVERE, "an error occurred while loading or parsing the configuration", e);
        }
    }

    /**
     * Startet den TimerTask und sonmit die Funktion des Kollektors
     */
    protected void start() {
        log.log(Level.FINE, "starting");
        Timer timer = new Timer(false);
        timer.scheduleAtFixedRate(this, delay * 1000, interval * 1000);
    }

    /**
     * @see java.util.TimerTask
     */
    public void run() {
        try {
            log.log(Level.FINE, "collecting");
            collect();
        } catch (Throwable t) {
            log.log(Level.SEVERE, "an error occurred while performing the collector task", t);
        }
    }

    /**
     * Spezifische Implementierung des Kollektors
     */
    public abstract void collect();

    /**
     * Gibt den zu verwendenden Logger für die reguläre Ausgabe zurück
     * @return der Logger
     */
    public Logger logger() {
        return logger != null ? Logger.getLogger(logger) : log;
    }
}
