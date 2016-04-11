package de.ivz.plm.logging;

import de.ivz.plm.logging.net.Connector;

import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class UdpHandler extends Handler {

    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private int remotePort;
    private String userFields;
    private String encodeCharset;
    private String version;
    private boolean syslog;

    private Connector connector;

    public UdpHandler() {
        super();
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (logRecord != null && isLoggable(logRecord)) {
            if (connector == null) {
                connector = new Connector(localAddress, localPort, remoteAddress, remotePort, encodeCharset);
            }
            Date timestamp = new Date(logRecord.getMillis());
            String logger = logRecord.getLoggerName();
            String level = logRecord.getLevel().toString();
            String thread = Integer.toString(logRecord.getThreadID());
            String clazz = logRecord.getSourceClassName();
            String method = logRecord.getSourceMethodName();
            String message = String.format(logRecord.getMessage(), logRecord.getParameters());
            String throwable = null;
            Throwable thrown = logRecord.getThrown();
            if (thrown != null && thrown.getStackTrace() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (StackTraceElement line : thrown.getStackTrace()) {
                    stringBuilder.append(line.toString()).append("\n");
                }
                throwable = stringBuilder.toString();
            }
            try {
                connector.send(syslog, version, timestamp, logger, level, thread, clazz, method, message, throwable, userFields);
            } catch (Exception e) {
                reportError("could not sent log data", e, ErrorManager.WRITE_FAILURE);
            }
        }
    }

    @Override
    public void close() {
        if (connector != null) {
            connector.close();
        }
    }

    @Override
    public void flush() {
        close();
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

    public boolean getSyslog() {
        return syslog;
    }

    public void setSyslog(boolean syslog) {
        this.syslog = syslog;
    }

}
