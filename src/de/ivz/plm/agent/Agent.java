package de.ivz.plm.agent;

import de.ivz.plm.util.option.StringOptionParser;

import java.lang.instrument.Instrumentation;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

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


    public Agent(String args, Instrumentation instrumentation) {
        // parse the options
        optionParser = new StringOptionParser(args);
        log.info(getClass().getSimpleName() + ": options=" + optionParser.getOptions());

        delay = optionParser.getLongOption(OPT_DELAY_NAME, OPT_DELAY_DEFAULT);
        interval = optionParser.getLongOption(OPT_INTERVAL_NAME, OPT_INTERVAL_DEFAULT);
        logger = optionParser.getStringOption(OPT_LOGGER_NAME , null);
    }

    public void start() {
        // third: initialize delayed components - jmxremote and meban
        if (delay > 0) {
            log.info(getClass().getSimpleName() + ": delayed components will be registered in " + delay + " seconds");
            new Timer().schedule(this, delay * 1000);
        } else {
            run();
        }

    }
}
