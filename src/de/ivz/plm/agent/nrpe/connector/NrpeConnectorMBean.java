package de.ivz.plm.agent.nrpe.connector;

import java.net.UnknownHostException;

public interface NrpeConnectorMBean {

    public void create() throws Exception;
    public void start() throws Exception;
    public void stop();
    public void destroy();

    public boolean isRunning();

    public String getBindAddress();
    public void setBindAddress(String bindAddress) throws UnknownHostException;

    public int getBindPort();
    public void setBindPort(int port);

    public int getReadTimeout();
    public void setReadTimeout(int timeout);

    public boolean getEnableTcpNoDelay();
    public void setEnableTcpNoDelay(boolean enableTcpNoDelay);

    public void setServerSocketFactory(String name) throws Exception;
    public String getServerSocketFactory();

    public String getCommand(String name);
    public String getCommands();
    public void setCommands(String commands);

    public String execute(String name, String[] args) throws Exception;
}
