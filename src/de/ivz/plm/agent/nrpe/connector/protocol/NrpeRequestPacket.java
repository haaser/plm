package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class NrpeRequestPacket extends NrpePacket implements Serializable {

     public NrpeRequestPacket(InputStream in) throws IOException, NrpeChecksumException {
        fromInputStream(in);
        validate();
    }
}
