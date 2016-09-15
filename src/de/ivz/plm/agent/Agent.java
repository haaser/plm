package de.ivz.plm.agent;

import com.sun.tools.attach.VirtualMachine;
import de.ivz.plm.util.option.StringOptionParser;

import java.lang.instrument.Instrumentation;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agent - Abstrakter Agent
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public abstract class Agent extends TimerTask {

    protected static final Logger log = Logger.getLogger(Agent.class.getName());

    public static final String OPT_DELAY_NAME       = "delay";
    public static final Long   OPT_DELAY_DEFAULT    = 0l;
    public static final String OPT_INTERVAL_NAME    = "interval";
    public static final Long   OPT_INTERVAL_DEFAULT = 60l;
    public static final String OPT_LOGGER_NAME      = "logger";

    protected StringOptionParser optionParser;
    protected Long delay;
    protected Long interval;
    protected String logger;


    /**
     * Konstruktor zur direkten Untertsüzung der Instrumentation-API
     * @param args Optionen für den Agenten - <I>javaagent:jarpath[=options]</I>
     * @param instrumentation Schnittstelle des Service zur Instrumentierung
     */
    public Agent(String args, Instrumentation instrumentation) {
        // parse the options
        optionParser = new StringOptionParser(args);
        log.info(getClass().getSimpleName() + ": options=" + optionParser.getOptions());

        delay = optionParser.getLongOption(OPT_DELAY_NAME, OPT_DELAY_DEFAULT);
        interval = optionParser.getLongOption(OPT_INTERVAL_NAME, OPT_INTERVAL_DEFAULT);
        logger = optionParser.getStringOption(OPT_LOGGER_NAME , null);
    }

    /**
     * Optional verzögerter Start von Komponenten und Ressourcen
     */
    public void start() {
        // third: initialize delayed components - jmxremote and meban
        if (delay > 0) {
            log.info(getClass().getSimpleName() + ": delayed components will be registered in " + delay + " seconds");
            new Timer().schedule(this, delay * 1000);
        } else {
            run();
        }

    }

    /**
     * Programm - Hauptmethode mit der Möglichkeit zum Laden eines Agenten-Jars während der Laufzeit
     * @param args Programm Optionen
     * @throws Exception wenn aufgetreten
     */
    public static void main(String[] args) throws Exception {
        if (args.length >= 2) {
            VirtualMachine vm = VirtualMachine.attach(args[0]);
            vm.loadAgent(args[1], args.length > 2 ? args[2] : "");
            vm.detach();
        } else {
            log.log(Level.SEVERE, "missing parameter");
        }
    }
}
