package de.ivz.plm.logging.net;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Connector - Versendet Nachrichten über das Netzwerk (UDP/TCP)
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class Connector {
    public final static SimpleDateFormat ISO_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public final static SimpleDateFormat SYSLOG_DATETIME_FORMAT = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US);
    public final static String DEFAULT_ENCODING = "UTF-8";

    private InetSocketAddress address;
    private DatagramSocket udpSocket;
    private Socket tcpSocket;

    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private int remotePort;
    private String localCharset;
    private String remoteCharset;
    private boolean encodeStrings;

    /**
     * Konstruktor mit netzwerkspezifischer Konfiguration
     * @param localAddress die Adresse, an die der Connector gebunden werden soll
     * @param localPort der Port, an den der Connector gebunden werden soll
     * @param remoteAddress die Adresse, an den der Connector senden soll
     * @param remotePort der Port, an den der Connector senden soll
     * @param encodeCharset das Encoding, welches beim Versand verwendet werrden soll
     */
    public Connector(String localAddress, int localPort, String remoteAddress, int remotePort, String encodeCharset) {
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        localCharset = System.getProperty("file.encoding");
        remoteCharset = encodeCharset != null ? encodeCharset : DEFAULT_ENCODING;
        encodeStrings = !remoteCharset.equalsIgnoreCase(localCharset);
        ISO_TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        SYSLOG_DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        //System.out.println("create connector from " + localAddress + ":" + localPort+ " to " + remoteAddress + ":" + remotePort);
    }

    /**
     * Initialisierung der UDP-Komponenten
     * @throws SocketException falls aufgetreten
     * @throws UnknownHostException falls aufgetreten
     */
    private void initUdp() throws SocketException, UnknownHostException {
        //System.out.println("init udp-connector from " + localAddress + ":" + localPort+ " to " + remoteAddress + ":" + remotePort);
        if (address == null) {
            address = new InetSocketAddress(remoteAddress, remotePort > 0 ? remotePort : 0);
        }
        if (localAddress != null) {
            udpSocket = new DatagramSocket(localPort > 0 ? localPort : 0, InetAddress.getByName(localAddress));
        } else {
            udpSocket = new DatagramSocket(localPort > 0 ? localPort : 0);
        }
    }

    /**
     * Initialisierung der TCP-Komponenten
     * @throws SocketException falls aufgetreten
     * @throws UnknownHostException falls aufgetreten
     */
    private void initTcp() throws IOException {
        //System.out.println("init tcp-connector from " + localAddress + ":" + localPort+ " to " + remoteAddress + ":" + remotePort);
        if (address == null) {
            address = new InetSocketAddress(remoteAddress, remotePort > 0 ? remotePort : 0);
        }
        if (localAddress != null) {
            tcpSocket = new Socket(address.getAddress(), address.getPort(), InetAddress.getByName(localAddress), localPort > 0 ? localPort : 0);
        } else {
            tcpSocket = new Socket(address.getAddress(), address.getPort(), InetAddress.getLocalHost(), localPort > 0 ? localPort : 0);
        }
        tcpSocket.setKeepAlive(true);
        tcpSocket.setSoTimeout(2500);
    }

    /**
     * Schließt alle netzwerkseitige Verbindungen
     */
    public void close() {
        if (udpSocket != null) {
            udpSocket.close();
        }
        if (tcpSocket != null) {
            try {
                tcpSocket.close();
            } catch (IOException e) {
                //System.out.println("error closing the tcp-socket: " + e.getMessage());
            }
        }
    }

    /**
     * Versendet den angegeben Text über das Netzwerk
     * @param text der text
     * @throws Exception falls aufgetreten
     */
    public void send(String text) throws Exception {
        if (text != null) {
            // UDP - RFC relevanter Text:
            // A field that specifies the length in bytes of the UDP header and UDP data.
            // The minimum length is 8 bytes since that's the length of the header.
            // The field size sets a theoretical limit of 65,535 bytes (8 byte header + 65,527 bytes of data) for a UDP datagram.
            // The practical limit for the data length which is imposed by the underlying IPv4 protocol is 65,507 bytes (65,535 − 8 byte UDP header − 20)
            if (text.getBytes().length <= 65507) {
                if (udpSocket == null) {
                    initUdp();
                }
                if (!udpSocket.isConnected()) {
                    udpSocket.connect(address);
                }
                DatagramPacket packet = new DatagramPacket(text.getBytes(), text.getBytes().length, address);
                //System.out.println("send udp-packet from " + udpSocket.getLocalAddress() + ":" + udpSocket.getLocalPort() + "  to " + packet.getAddress() + ":" + packet.getPort() + " with " + packet.getLength() + "bytes of data -> " + data);
                udpSocket.send(packet);
            } else {
                if (tcpSocket == null || tcpSocket.isClosed()) {
                    initTcp();
                }
                if (!tcpSocket.isConnected()) {
                    tcpSocket.connect(address);
                }
                //System.out.println("send tcp-packet from " + tcpSocket.getLocalAddress() + ":" + tcpSocket.getLocalPort() + "  to " + tcpSocket.getRemoteSocketAddress() + " with " + data.length() + "bytes of data -> " + data);
                tcpSocket.getOutputStream().write(text.getBytes());
                tcpSocket.getOutputStream().flush();
            }
        }
    }


    /**
     * Bereitet die Log-Daten für den VErsand auf und verendet diese dann über das Netzwerk
     * @param syslog zum Syslog kompatible Ausgabe
     * @param version einzusetzende Version
     * @param timestamp Log-Daten: Zeitstempel
     * @param logger Log-Daten: Quelle
     * @param level Log-Daten: Priorität
     * @param thread Log-Daten: Thread
     * @param clazz Log-Daten: Klasse
     * @param method Log-Daten: Methode
     * @param message Log-Daten: Nachricht
     * @param throwable Log-Daten: Throwable (Ursache)
     * @param userFields einzusetzende zusätzliche Daten
     * @throws Exception falls aufgetreten
     */
    public void send(boolean syslog, String version, Date timestamp, String logger, String level, String thread, String clazz, String method, String message, String throwable, String userFields) throws Exception {
        String text = null;
            LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
            data.put("@version", version != null ? version : "1");
            data.put("@timestamp", format2JsonDate(timestamp));
            if (userFields != null) {
                for (String userField : userFields.split(",")) {
                    String[] fieldData = userField.trim().split(":", 2);
                    if (fieldData.length == 2) {
                        String key = fieldData[0].trim();
                        String value = fieldData[1];
                        if (value.startsWith("${") && value.endsWith("}")) {
                            value = System.getProperty(value.substring(2, value.length() - 1), value);
                        }
                        data.put(key, value != null ? value.trim() : null);
                    }
                }
            }
            data.put("logger", logger);
            data.put("thread", thread);
            data.put("level", level);
            data.put("class", clazz);
            data.put("method", method);
            data.put("message", encodeString(message));
            if (throwable != null) {
                data.put("throwable", encodeString(throwable));
            }
            if (syslog) {
                text = format2SyslogDate(timestamp);
                text += " ";
                text += (localAddress != null ? localAddress : InetAddress.getLocalHost().getCanonicalHostName());
                text += " BG: -:-:-:";
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    text += encodeSyslogString(entry.getKey());
                    text += "=";
                    text += encodeSyslogString(entry.getValue());
                    text += ";";
                }
                text += "\n";
            } else {
                text = JSONObject.toJSONString(data);
            }
        send(text);
    }

    /**
     * Formatiert das angegebene Datum nach ISO 8601 (JSON)
     * @param date das Datum
     * @return das nach ISO 8601 formatierte Datum
     */
    public String format2JsonDate(Date date) {
        synchronized (ISO_TIMESTAMP_FORMAT) {
            return ISO_TIMESTAMP_FORMAT.format(date);
        }
    }

    /**
     * Formatiert das angegebene Datum zum Syslog kompatibel
     * @param date das Datum
     * @return das zum Syslog kompatibel formatierte Datum
     */
    public String format2SyslogDate(Date date) {
        synchronized (SYSLOG_DATETIME_FORMAT) {
            return SYSLOG_DATETIME_FORMAT.format(date);
        }
    }

    /**
     * Encodiert einen String in das anzuwendende Encoding
     * @param value der originale String
     * @return der encodierte String
     */
    public String encodeString(String value) {
        if (value != null && encodeStrings) {
            try {
                return new String(value.getBytes(remoteCharset), localCharset);
            } catch (java.io.UnsupportedEncodingException e) {
                // ignore
                //e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * Encodiert den String zum Sysog kompatibel
     * @param value der oroginale String
     * @return der encodierte String
     */
    public String encodeSyslogString(String value) {
        if (value != null) {
            value = JSONObject.escape(value.replaceAll(";", "\\;").replaceAll("=", "\\="));
        }
        return value;
    }
}
