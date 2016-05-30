package de.ivz.plm.agent.nrpe.connector.invoker;

import bsh.EvalError;
import bsh.Interpreter;
import de.ivz.plm.agent.nrpe.connector.SocketHandler;
import de.ivz.plm.agent.nrpe.connector.protocol.NrpePacket;
import de.ivz.plm.agent.nrpe.connector.protocol.NrpeReturnValue;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandInvoker {

    protected static final Logger log = Logger.getLogger(SocketHandler.class.getName());

    private String code;
    private MBeanInvoker mbean;

    public CommandInvoker(String code, MBeanInvoker mbean) {
        this.code = code;
        this.mbean = mbean;
    }

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
