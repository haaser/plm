package de.ivz.plm.agent.nrpe;

import de.ivz.plm.agent.Agent;
import de.ivz.plm.agent.nrpe.connector.NrpeConnector;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;

/**
 * NrpeAgent - Spezifischer Agent zur Bereitstellung eier NRPE-Schnittstelle
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class NrpeAgent extends Agent {

    public static final String  OPTION_BIND_ADDRESS = "bindaddress";
    public static final String  OPTION_BIND_PORT    = "bindport";
    public static final String  OPTION_READ_TIMEOUT = "readtimeout";
    public static final String  OPTION_TCP_NO_DELAY = "tcpnodelay";
    public static final String  OPTION_COMMANDS     = "commands";

    private final NrpeConnector nrpeConnector;

    /**
     * @see de.ivz.plm.agent.Agent
     */
    public NrpeAgent(String args, Instrumentation instrumentation) {
        // first: initialize super-agent
        super(args, instrumentation);

        // second: initialize NrpeConnector
        nrpeConnector = new NrpeConnector();
        try {
            nrpeConnector.create();
            nrpeConnector.setBindAddress(optionParser.getStringOption(OPTION_BIND_ADDRESS, NrpeConnector.OPTION_BIND_ADDRESS_DEFAULT));
            nrpeConnector.setBindPort(optionParser.getIntegerOption(OPTION_BIND_PORT, NrpeConnector.OPTION_BIND_PORT_DEFAULT));
            nrpeConnector.setReadTimeout(optionParser.getIntegerOption(OPTION_READ_TIMEOUT, NrpeConnector.OPTION_READ_TIMEOUT_DEFAULT));
            nrpeConnector.setEnableTcpNoDelay(optionParser.getBooleanOption(OPTION_TCP_NO_DELAY, NrpeConnector.OPTION_TCP_NO_DELAY_DEFAULT));
            nrpeConnector.setCommands(optionParser.getStringOption(OPTION_COMMANDS, NrpeConnector.OPTION_COMMANDS_DEFAULT));
            nrpeConnector.start();
        } catch (Exception e) {
            log.log(Level.SEVERE, "an error occurred while initializing the NrpeConnector", e);
        }

        // third: initialize delayed components
        start();
    }

    /**
     * @see de.ivz.plm.agent.Agent
     */
    @Override
    public void run() {
        // register collector to local mbeanserver
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            mbeanServer.registerMBean(nrpeConnector, new ObjectName("ivz.plm:type=NrpeConnector"));
            log.info("NrpeConnector registered to platform mbean server");
        } catch (Exception e) {
            log.log(Level.WARNING, "NrpeConnector could not register to platform mbean server", e);
        }
    }

    /**
     * Hauptmethode zum Starten des Agenten mit der Laufzeit
     * @param args Optionen für den Agenten - <I>javaagent:jarpath[=options]</I>
     * @param instrumentation Schnittstelle des Service zur Instrumentierung
     */
    public static void premain(String args, Instrumentation instrumentation) {
        new NrpeAgent(args, instrumentation);
    }

    /**
     * Hauptmethode zum Starten des Agenten während der Laufzeit
     * @param args Optionen für den Agenten - <I>com.sun.tools.attach.VirtualMachine: loadAgent(jarpath, options)</I>
     * @param instrumentation Schnittstelle des Service zur Instrumentierung
     */
    public static void agentmain(String args, Instrumentation instrumentation) {
        new NrpeAgent(args, instrumentation);
    }
}
