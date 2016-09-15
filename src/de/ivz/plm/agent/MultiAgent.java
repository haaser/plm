package de.ivz.plm.agent;

import de.ivz.plm.agent.nrpe.NrpeAgent;
import de.ivz.plm.agent.statistic.StatisticAgent;

import java.lang.instrument.Instrumentation;

/**
 * MultiAgent - Zusammenfassender Agent zum einfacheren Einbinden
 * @see de.ivz.plm.agent.statistic.StatisticAgent
 * @see de.ivz.plm.agent.nrpe.NrpeAgent
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class MultiAgent extends Agent {

    /**
     * @see de.ivz.plm.agent.Agent
     */
    public MultiAgent(String args, Instrumentation instrumentation) {
        // first: initialize super-agent
        super(args, instrumentation);

        // second: initialize sub-agents
        if (optionParser.getBooleanOption("statistic", false)) {
            new StatisticAgent(args, instrumentation);
        }

        if (optionParser.getBooleanOption("nrpe", false)) {
            new NrpeAgent(args, instrumentation);
        }

        // third: initialize delayed components
        start();
    }

    /**
     * @see de.ivz.plm.agent.Agent
     */
    @Override
    public void run() {
        // nothing to do ;)
    }

    /**
     * Hauptmethode zum Starten des Agenten mit der Laufzeit
     * @param args Optionen für den Agenten - <I>javaagent:jarpath[=options]</I>
     * @param instrumentation Schnittstelle des Service zur Instrumentierung
     */
    public static void premain(String args, Instrumentation instrumentation) {
        new MultiAgent(args, instrumentation);
    }

    /**
     * Hauptmethode zum Starten des Agenten während der Laufzeit
     * @param args Optionen für den Agenten - <I>com.sun.tools.attach.VirtualMachine: loadAgent(jarpath, options)</I>
     * @param instrumentation Schnittstelle des Service zur Instrumentierung
     */
    public static void agentmain(String args, Instrumentation instrumentation) {
        new MultiAgent(args, instrumentation);
    }

}
