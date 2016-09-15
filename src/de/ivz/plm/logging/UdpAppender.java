package de.ivz.plm.logging;

import de.ivz.plm.logging.net.Connector;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Date;

/**
 * UdpAppender - Erweiterung für das Logging-Framework "Log4j"
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class UdpAppender extends AppenderSkeleton {

    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private int remotePort;
    private String userFields;
    private String encodeCharset;
    private String version;
    private boolean syslog;

    private Connector connector;

    /**
     * Konstruktor
     */
    public UdpAppender() {
        super();
    }

    /**
     * Überführt die Asugabe des framework-spezifischen Logging-Objekt an das Netzwerk
     * @see java.util.logging.Handler
     */
    @Override
    protected void append(LoggingEvent loggingEvent) {
        // Nur "append", wenn der "loggingEvent" gültig und ausgabefähig ist
        if (loggingEvent != null && isAsSevereAsThreshold(loggingEvent.getLevel())) {
            // Initialisiere bei Bedarf den Connector
            if (connector == null) {
                connector = new Connector(localAddress, localPort, remoteAddress, remotePort, encodeCharset);
            }
            // Lese Daten vom "logRecord" ein
            Date timestamp = new Date(loggingEvent.timeStamp);
            String logger = loggingEvent.getLoggerName();
            String level = loggingEvent.getLevel().toString();
            String thread = loggingEvent.getThreadName();
            String clazz = loggingEvent.getLocationInformation().getClassName();
            String method = loggingEvent.getLocationInformation().getMethodName();
            String message = loggingEvent.getRenderedMessage();
            String throwable = null;
            // Lese den kompletten Stacktrace des Throwable ein
            ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
            if (throwableInformation != null && throwableInformation.getThrowableStrRep() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String line : loggingEvent.getThrowableInformation().getThrowableStrRep()) {
                    stringBuilder.append(line).append("\n");
                }
                throwable = stringBuilder.toString();
            }
            try {
                // versende die Daten über den Connector
                connector.send(syslog, version, timestamp, logger, level, thread, clazz, method, message, throwable, userFields);
            } catch (Exception e) {
                // melde interne Fehler über den speziellen Log-Logger
                LogLog.error("could not sent log data", e);
            }
        }
    }

    /**
     * @see org.apache.log4j.Appender
     */
    public void close() {
        if (connector != null) {
            connector.close();
        }
    }

    /**
     * @see org.apache.log4j.Appender
     */
    public boolean requiresLayout() {
        return false;
    }

    /**
     * Gibt die Adresse zurück, auf die der Connector gebunden ist
     * @return die Adresse
     */
    public String getLocalAddress() {
        return localAddress;
    }

    /**
     * Setzt die Adresse, an die der Connector gebunden werden soll
     * @param localAddress die zu setzende Adresse
     */
    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Gibt den Port zurück, auf die der Connector gebunden ist
     * @return der Port
     */
    public int getLocalPort() {
        return localPort;
    }

    /**
     * Setzt den Port, an den der Connector gebunden werden soll
     * @param localPort der zu setzende Port
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    /**
     * Gibt die Adresse zurück, an die der Connector sendet
     * @return die Adresse
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Setzt die Adresse, an die der Connector senden soll
     * @param remoteAddress die zu setzende Adresse
     */
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Gibt den Port zurück, an den der Connector sendet
     * @return der Port
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Setzt den Port, an den der Connector senden soll
     * @param remotePort der zu setzende Port
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * Gibt die zusätzlichen Daten (UserFields) zurück, welche den Log-Daten hinzugefügt werden
     * @return die UserFields
     */
    public String getUserFields() {
        return userFields;
    }

    /**
     * Setzt die zusätzlichen Daten (UserFields), welche den Log-Daten hinzugefügt werden sollen
     * @param userFields die zu setzenden UserFields
     */
    public void setUserFields(String userFields) {
        this.userFields = userFields;
    }

    /**
     * Gibt das zu verwendende Encoding der Log-Daten beim Versand an
     * @return das Encoding
     */
    public String getEncodeCharset() {
        return encodeCharset;
    }

    /**
     * Setzt das Encoding, welches beim Versand der Log-Daten verwendet werden soll
     * @param encodeCharset das zu setzende Encoding
     */
    public void setEncodeCharset(String encodeCharset) {
        this.encodeCharset = encodeCharset;
    }

    /**
     * Gibt die Version an, welche als Angabe den Log-Daten hinzugefügt wird
     * @return die Version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setzt die Version, welche als Angabe den Log-Daten hinzugefügt wird
     * @param version die zu setzende Version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gibt an, ob eine zum Syslog kompatible Ausgabe gemacht wird
     * @return Status der Ausgabe
     */
    public boolean getSyslog() {
        return syslog;
    }

    /**
     * Setzt den Status der zum Syslog kompatiblen Ausgabe
     * @param syslog der zu setzende Status
     */
    public void setSyslog(boolean syslog) {
        this.syslog = syslog;
    }

}
