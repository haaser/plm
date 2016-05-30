package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.Serializable;

public class NrpeChecksumException extends Exception implements Serializable {

    public NrpeChecksumException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public NrpeChecksumException(String message) {
        super(message);
    }

    public NrpeChecksumException(Throwable throwable) {
        super(throwable);
    }
}
