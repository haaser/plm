package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.Serializable;

/**
 * NrpeReturnValue
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class NrpeReturnValue implements Serializable {

    private int code;
    private String message;

    /**
     * Konstruktor für den Rückgabe wert
     * @param code der Code
     * @param message die Nachricht
     */
    public NrpeReturnValue(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Gibt den Code des Rückgabewerts an
     * @return der Code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gibt die Nachricht des Rückgabewerts aus
     * @return die Nachricht
     */
    public String getMessage() {
        return message;
    }

    /**
     * Erzeugt einene repräsentative Ausgabe für das Objekt
     * @return die repräsentative Ausgabe
     */
    public String toString() {
        return "NrpeReturnValue[code=" + code + ", message='" + message + "']";
    }

}
