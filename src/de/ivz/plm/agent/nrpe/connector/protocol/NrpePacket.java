package de.ivz.plm.agent.nrpe.connector.protocol;

import java.io.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class NrpePacket implements Serializable {

    protected static final Logger log = Logger.getLogger(NrpePacket.class.getName());

    public static final String VERSION = "0.1/2.0";
    public static final int TYPE_QUERY = 1;
    public static final int TYPE_RESPONSE = 2;
    public static final int VERSION_1 = 1;
    public static final int VERSION_2 = 2;
    public static final int CODE_OK = 0;
    public static final int CODE_WARNING = 1;
    public static final int CODE_CRITICAL = 2;
    public static final int CODE_UNKNOWN = 3;
    public static final int BUFFER_SIZE = 1024;


    private int version, type, code;
    private long crc;
    private byte buffer[], dummy[];

    NrpePacket() {
        crc = 0;
        type = 0;
        version = 0;
        code = CODE_OK;
        buffer = new byte[BUFFER_SIZE];
        dummy = new byte[2];
    }

    public long getCRC() {
        return crc;
    }

    public void setCRC(long crc) {
        this.crc = crc;
    }

    public int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        int zeroIndex = BUFFER_SIZE - 1;
        for (int i = 0; i < BUFFER_SIZE; i++) {
            if (buffer[i] != 0) continue;
            zeroIndex = i;
            break;
        }
        return new String(buffer, 0, zeroIndex);
    }

    protected void setMessage(String message) {
        if (message == null) message = "";
        System.arraycopy(message.getBytes(), 0, buffer, 0, Math.min(message.length(), BUFFER_SIZE));
        if (message.length() < BUFFER_SIZE) buffer[message.length()] = 0;
    }

    protected void fromInputStream(InputStream input) throws IOException {
        DataInputStream din = new DataInputStream(input);
        version = din.readUnsignedShort();
        type = din.readUnsignedShort();
        crc = din.readInt() & 0xffffffffL;
        code = din.readShort();
        din.readFully(buffer);
        din.readFully(dummy);
        log.log(Level.FINE, "packet[version=" + version + ", type=" + type + ", crc=" + crc + ", code=" + code + ", message='" + getMessage() + "']");
    }

    public void validate() throws NrpeChecksumException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeShort(version);
            dos.writeShort(type);
            dos.writeInt(0);
            dos.writeShort(code);
            dos.write(buffer);
            dos.write(dummy);
            dos.close();
            byte bytes[] = baos.toByteArray();
            CRC32 crcAlg = new CRC32();
            crcAlg.update(bytes);
            if (crcAlg.getValue() != crc) throw new NrpeChecksumException("found='" + crc + "', calculated='" + crcAlg.getValue() + "'");
        } catch (IOException ioexception) {
        }
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeShort(version);
            dos.writeShort(type);
            dos.writeInt((int)crc);
            dos.writeShort(code);
            dos.write(buffer);
            dos.write(dummy);
            dos.close();
        } catch (IOException ioexception) {
        }
        return baos.toByteArray();
    }

    protected void initRandomBuffer() {
        Random r = new Random(System.currentTimeMillis());
        r.nextBytes(buffer);
        r.nextBytes(dummy);
    }

}
