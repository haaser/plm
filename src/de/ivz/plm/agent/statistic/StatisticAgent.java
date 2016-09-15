package de.ivz.plm.agent.statistic;

import de.ivz.plm.agent.Agent;
import de.ivz.plm.agent.statistic.collector.StatisticCollector;
import de.ivz.plm.agent.statistic.transfomer.ClassStatisticTransformer;
import de.ivz.plm.agent.statistic.transfomer.EjbStatisticTransformer;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;

/**
 * StatisticAgent - Spezifischer Agent zur Durchführung der Laufzeitmessung
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class StatisticAgent extends Agent {

    public static final String  OPT_FULLEVERY_NAME    = "fullevery";
    public static final Long    OPT_FULLEVERY_DEFAULT = -1l;
    public static final String  OPT_FULLREG_NAME      = "fullreg";
    public static final Boolean OPT_FULLREG_DEFAULT   = false;
    public static final String  OPT_COMPACT_NAME      = "compact";
    public static final Boolean OPT_COMPACT_DEFAULT   = false;

    private final StatisticCollector statisticCollector;

    /**
     * @see de.ivz.plm.agent.Agent
     */
    public StatisticAgent(String args, Instrumentation instrumentation) {
        // first: initialize super-agent
        super(args, instrumentation);

        // second: initialize StatisticCollector
        statisticCollector = new StatisticCollector();
        try {
            statisticCollector.create();
            statisticCollector.setInterval(interval);
            statisticCollector.setLogger(logger);
            statisticCollector.setFullevery(optionParser.getLongOption(OPT_FULLEVERY_NAME, OPT_FULLEVERY_DEFAULT));
            statisticCollector.setFullreg(optionParser.getBooleanOption(OPT_FULLREG_NAME, OPT_FULLREG_DEFAULT));
            statisticCollector.setCompact(optionParser.getBooleanOption(OPT_COMPACT_NAME, OPT_COMPACT_DEFAULT));
            statisticCollector.start();
        } catch (Exception e) {
            log.log(Level.SEVERE, "an error occurred while initializing the database", e);
            // ignore
        }

        // third: registers simple class transformer
        if (optionParser.hasOption("classes")) {
            instrumentation.addTransformer(new ClassStatisticTransformer(optionParser.getStringOption("classes", "^$")));
            log.info("ClassStatisticTransformer registered");
        }
        // fourth: registers extended ejb transformer
        if (optionParser.hasOption("ejbs")) {
            instrumentation.addTransformer(new EjbStatisticTransformer(optionParser.getStringOption("ejbs", "^$")));
            log.info("EjbStatisticTransformer registered");
        }

        // fifth: initialize delayed components
        start();
    }

    /**
     *@see de.ivz.plm.agent.Agent
     */
    public void run() {
        // register collector to local mbeanserver
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            mbeanServer.registerMBean(statisticCollector, new ObjectName("ivz.plm:type=StatisticCollector"));
            log.info("StatisticCollector registered to platform mbean server");
        } catch (Exception e) {
            log.log(Level.WARNING, "StatisticCollector could not register to platform mbean server", e);
        }
    }

    /**
     * Hauptmethode zum Starten des Agenten mit der Laufzeit
     * @param args Optionen für den Agenten - <I>javaagent:jarpath[=options]</I>
     * @param instrumentation Schnittstelle des Service zur Instrumentierung
     */
    public static void premain(String args, Instrumentation instrumentation) {
        new StatisticAgent(args, instrumentation);
    }

    /**
     * Hauptmethode zum Starten des Agenten während der Laufzeit
     * @param args Optionen für den Agenten - <I>com.sun.tools.attach.VirtualMachine: loadAgent(jarpath, options)</I>
     * @param instrumentation Schnittstelle des Service zur Instrumentierung
     */
    public static void agentmain(String args, Instrumentation instrumentation) {
        new StatisticAgent(args, instrumentation);
    }

}
