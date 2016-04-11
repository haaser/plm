package de.ivz.plm.logging;

import de.ivz.plm.logging.net.Connector;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Date;

public class UdpAppender extends AppenderSkeleton implements Appender {

    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private int remotePort;
    private String userFields;
    private String encodeCharset;
    private String version;
    private boolean syslog;

    private Connector connector;

    public UdpAppender() {
        super();
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (loggingEvent != null && isAsSevereAsThreshold(loggingEvent.getLevel())) {
            if (connector == null) {
                connector = new Connector(localAddress, localPort, remoteAddress, remotePort, encodeCharset);
            }
            Date timestamp = new Date(loggingEvent.timeStamp);
            String logger = loggingEvent.getLoggerName();
            String level = loggingEvent.getLevel().toString();
            String thread = loggingEvent.getThreadName();
            String clazz = loggingEvent.getLocationInformation().getClassName();
            String method = loggingEvent.getLocationInformation().getMethodName();
            String message = loggingEvent.getRenderedMessage();
            String throwable = null;
            ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
            if (throwableInformation != null && throwableInformation.getThrowableStrRep() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String line : loggingEvent.getThrowableInformation().getThrowableStrRep()) {
                    stringBuilder.append(line).append("\n");
                }
                throwable = stringBuilder.toString();
            }
            try {
                connector.send(syslog, version, timestamp, logger, level, thread, clazz, method, message, throwable, userFields);
            } catch (Exception e) {
                LogLog.error("could not sent log data", e);
            }
        }
    }

    public void close() {
        if (connector != null) {
            connector.close();
        }
    }

    public boolean requiresLayout() {
        return false;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getUserFields() {
        return userFields;
    }

    public void setUserFields(String userFields) {
        this.userFields = userFields;
    }

    public String getEncodeCharset() {
        return encodeCharset;
    }

    public void setEncodeCharset(String encodeCharset) {
        this.encodeCharset = encodeCharset;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean getSyslog() { return syslog; }

    public void setSyslog(boolean syslog) { this.syslog = syslog; }
}
