package de.ivz.plm.agent;

import de.ivz.plm.agent.nrpe.NrpeAgent;
import de.ivz.plm.agent.statistic.StatisticAgent;
import de.ivz.plm.util.option.StringOptionParser;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

public class MultiAgent extends Agent {

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

    @Override
    public void run() {
        // nothing to do ;)
    }

    public static void premain(String args, Instrumentation instrumentation) {
        new MultiAgent(args, instrumentation);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        new MultiAgent(args, instrumentation);
    }

}
