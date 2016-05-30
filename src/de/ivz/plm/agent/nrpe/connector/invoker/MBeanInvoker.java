package de.ivz.plm.agent.nrpe.connector.invoker;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MBeanInvoker {

    private final static Logger log = Logger.getLogger(MBeanInvoker.class.getName());

    public Object invoke(String name, String method, Object[] params, String[] signature) throws Exception {
        try {
             return getLocalServer().invoke(new ObjectName(name), method, params, signature);
        } catch (Exception e) {
            log.log(Level.SEVERE, "could not invoke mbean name='" + name + ", method='" + method + "', params='" + Arrays.asList(params) + "', signature='" + Arrays.asList(signature) + "'", e);
            throw e;
        }
    }

    public Object attribute(String name, String attribute) throws Exception {
        try {
            return getLocalServer().getAttribute(new ObjectName(name), attribute);
        } catch (Exception e) {
            log.log(Level.SEVERE, "could not query mbean name='" + name + ", attribute='" + attribute + "'", e);
            throw e;
        }
    }

    private MBeanServer getLocalServer() throws Exception {
        for (MBeanServer server : javax.management.MBeanServerFactory.findMBeanServer(null)) {
            if ("DefaultDomain".equals(server.getDefaultDomain())) {
                return server;
            }
        }
        throw new Exception("Failed to locate MBeanServer");
    }
}
