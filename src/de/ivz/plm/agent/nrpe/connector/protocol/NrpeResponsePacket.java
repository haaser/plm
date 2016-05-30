package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.Serializable;
import java.util.zip.CRC32;

public class NrpeResponsePacket extends NrpePacket implements Serializable {

    public NrpeResponsePacket() {
        setType(TYPE_RESPONSE);
        setVersion(VERSION_2);
        setCode(CODE_UNKNOWN);
    }

    public void updateCRC() {
        int crc = 0;
        setCRC(crc);
        CRC32 crc32 = new CRC32();
        crc32.update(toByteArray());
        crc = (int)crc32.getValue();
        setCRC(crc);
    }

    public void setMessage(String sMessage) {
        initRandomBuffer();
        super.setMessage(sMessage);
    }
}