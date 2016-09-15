package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.Serializable;

/**
 * NrpeChecksumException
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class NrpeChecksumException extends Exception implements Serializable {

    /**
     * Konstruktor mit der Nachricht und der Ursache
     * @param message die Nachricht
     * @param throwable die Ursache
     */
    public NrpeChecksumException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Konstruktor nur mit der
     * @param message die Nachricht
     */
    public NrpeChecksumException(String message) {
        super(message);
    }

    /**
     * Konstruktor nur mit der Ursache
     * @param throwable die Ursache
     */
    public NrpeChecksumException(Throwable throwable) {
        super(throwable);
    }
}
