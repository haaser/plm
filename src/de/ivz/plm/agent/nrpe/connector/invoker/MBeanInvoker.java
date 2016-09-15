package de.ivz.plm.agent.nrpe.connector.invoker;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MBeanInvoker - Kapselt die Logik zur Ermittlung des MBeanServers und der Ausführung von Methoden
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class MBeanInvoker {

    private final static Logger log = Logger.getLogger(MBeanInvoker.class.getName());

    /**
     * Führt die Methode einer MBean aus
     * @param name Name der MBean
     * @param method Name der Methode
     * @param params Parameter der Methode
     * @param signature Signatur der Parameter
     * @return Rückgabe/Object der ausgeführten Methode
     * @throws Exception falls aufgetreten
     */
    public Object invoke(String name, String method, Object[] params, String[] signature) throws Exception {
        try {
             return getLocalServer().invoke(new ObjectName(name), method, params, signature);
        } catch (Exception e) {
            log.log(Level.SEVERE, "could not invoke mbean name='" + name + ", method='" + method + "', params='" + Arrays.asList(params) + "', signature='" + Arrays.asList(signature) + "'", e);
            throw e;
        }
    }

    /**
     * Ermittelt das Attribut einer MBean
     * @param name Name der MBean
     * @param attribute Name des Attributes
     * @return das Attribute
     * @throws Exception falls aufgetreten
     */
    public Object attribute(String name, String attribute) throws Exception {
        try {
            return getLocalServer().getAttribute(new ObjectName(name), attribute);
        } catch (Exception e) {
            log.log(Level.SEVERE, "could not query mbean name='" + name + ", attribute='" + attribute + "'", e);
            throw e;
        }
    }

    /**
     * Interne Methode zur Ermittlung des lokalen MBeanServers
     * @return der MBeanServer
     * @throws Exception falls aufgetreten
     */
    private MBeanServer getLocalServer() throws Exception {
        for (MBeanServer server : javax.management.MBeanServerFactory.findMBeanServer(null)) {
            if ("DefaultDomain".equals(server.getDefaultDomain())) {
                return server;
            }
        }
        throw new Exception("Failed to locate MBeanServer");
    }
}
