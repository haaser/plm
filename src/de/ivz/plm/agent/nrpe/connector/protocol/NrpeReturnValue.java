package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.Serializable;

public class NrpeReturnValue implements Serializable {

    private int code;
    private String message;

    public NrpeReturnValue(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return "NrpeReturnValue[code=" + code + ", message='" + message + "']";
    }

}
