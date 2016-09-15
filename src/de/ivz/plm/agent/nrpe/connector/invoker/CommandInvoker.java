package de.ivz.plm.agent.nrpe.connector.invoker;

import bsh.EvalError;
import bsh.Interpreter;
import de.ivz.plm.agent.nrpe.connector.SocketHandler;
import de.ivz.plm.agent.nrpe.connector.protocol.NrpePacket;
import de.ivz.plm.agent.nrpe.connector.protocol.NrpeReturnValue;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CommandInvoker - Kapselt die Code-Umgebng und die Ausführung
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class CommandInvoker {

    protected static final Logger log = Logger.getLogger(SocketHandler.class.getName());

    private String code;
    private MBeanInvoker mbean;

    /**
     * Konstruktor mit BSH-Code und dem zu verwendenden MBeanInvoker
     * @param code der Code
     * @param mbean der MBeanInvoker
     */
    public CommandInvoker(String code, MBeanInvoker mbean) {
        this.code = code;
        this.mbean = mbean;
    }

    /**
     * Führt den Code mit Angabe der zu verwendenden Parameter aus und erzeugt eine Ergebnis
     * @param arguments die Parameter
     * @return das Ergebnis
     * @throws EvalError falls aufgetreten
     */
    public NrpeReturnValue invoke(String[] arguments) throws EvalError {
        NrpeReturnValue returnValue;
        if (code != null) {
            try {
                Interpreter i = new Interpreter();
                i.setClassLoader(getClass().getClassLoader());
                i.setStrictJava(true);
                i.set("arguments", arguments != null && arguments.length > 0 ? arguments[0] : "");
                i.set("code", NrpePacket.CODE_UNKNOWN);
                i.set("message", "");
                i.set("mbean", mbean);
                i.eval(code);
                returnValue = new NrpeReturnValue((Integer)i.get("code"), (String)i.get("message"));
            } catch (Exception e) {
                log.log(Level.SEVERE, "could not invoke", e);
                returnValue = new NrpeReturnValue(NrpePacket.CODE_CRITICAL, "could not invoke code: " + e.getMessage().replaceAll("\n", ""));
            }
        } else {
            returnValue = new NrpeReturnValue(NrpePacket.CODE_UNKNOWN, "undefined code (null)");
        }
        return returnValue;
    }

}
