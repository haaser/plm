package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * NrpeRequestPacket
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class NrpeRequestPacket extends NrpePacket implements Serializable {

    /**
     * Konstruktor für das Packet anhand der Daten aus dem InputStreams
     * @param in der InputStream
     * @throws IOException falls aufgetreten
     * @throws NrpeChecksumException falls aufgetreten
     */
    public NrpeRequestPacket(InputStream in) throws IOException, NrpeChecksumException {
        fromInputStream(in);
        validate();
    }
}
