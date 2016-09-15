package de.ivz.plm.agent.nrpe.connector;

import de.ivz.plm.agent.nrpe.connector.invoker.CommandInvoker;
import de.ivz.plm.agent.nrpe.connector.invoker.MBeanInvoker;
import de.ivz.plm.agent.nrpe.connector.protocol.NrpeReturnValue;
import de.ivz.plm.util.text.StringUtil;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NrpeConnector als Managed Bean (Bean Implementation)
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class NrpeConnector implements Runnable, NrpeConnectorMBean {

    protected static final Logger log = Logger.getLogger(NrpeConnector.class.getName());

    public static final String  OPTION_BIND_ADDRESS_DEFAULT           = "0.0.0.0";
    public static final Integer OPTION_BIND_PORT_DEFAULT              = 0;
    public static final Integer OPTION_READ_TIMEOUT_DEFAULT           = 0;
    public static final Boolean OPTION_TCP_NO_DELAY_DEFAULT           = false;
    public static final String  OPTION_COMMANDS_DEFAULT               = "jvm,threads,memory,os,hprof";
    public static final Integer OPTION_THREAD_POOL_SIZE_DEFAULT       = 10;
    public static final Integer OPTION_THREAD_KEEP_ALIVE_TIME_DEFAULT = 600;


    private InetAddress bindAddress = null;
    private int bindPort = OPTION_BIND_PORT_DEFAULT;
    private int readTimeout = OPTION_READ_TIMEOUT_DEFAULT;
    private boolean enableTcpNoDelay = OPTION_TCP_NO_DELAY_DEFAULT;

    private String commands;
    private Map<String, String> commandMap = new LinkedHashMap<String, String>();

    private boolean running = false;
    private ServerSocketFactory serverSocketFactory;
    private ServerSocket serverSocket;
    private ThreadPoolExecutor threadPoolExecutor;
    private Thread acceptThread;

    public void create() {
        log.log(Level.INFO, "NrpeConnector create");
        if (serverSocketFactory == null) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        }
    }

    public void start() throws Exception {
        log.info("NrpeConnector start");
        // Use the default javax.protocol.ServerSocketFactory if none was set
        serverSocket = serverSocketFactory.createServerSocket(bindPort, 50, bindAddress);
        InetAddress socketAddress = serverSocket.getInetAddress();
        log.info("NrpeConnector available at : " + socketAddress + ":" + serverSocket.getLocalPort());
        threadPoolExecutor = new ThreadPoolExecutor(OPTION_THREAD_POOL_SIZE_DEFAULT, OPTION_THREAD_POOL_SIZE_DEFAULT, OPTION_THREAD_KEEP_ALIVE_TIME_DEFAULT, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        acceptThread = new Thread(Thread.currentThread().getThreadGroup(), this, "NrpeConnector Accept Thread");
        running = true;
        acceptThread.start();
    }

    public void stop() {
        log.log(Level.INFO, "NrpeService stop");
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
        try {
            running = false;
            // interrupt Accept Thread
            if (acceptThread != null) {
                acceptThread.interrupt();
            }
            // unbind Server Socket if needed
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception occurred when trying to stop NrpeService: ", e);
        }
    }

    public void destroy() {
        log.log(Level.INFO, "NrpeConnector destroy");
        commands = null;
        commandMap.clear();
        acceptThread = null;
        threadPoolExecutor = null;
        serverSocket = null;
        serverSocketFactory = null;
    }

    public void run() {
        log.log(Level.INFO, "NrpeConnector run");
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                SocketHandler handler = new SocketHandler(socket, this);
                threadPoolExecutor.submit(handler);
            } catch (IOException e) {
                log.log(Level.SEVERE, "could not accept client", e);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getBindPort() {
        return bindPort;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public String getBindAddress() {
        return bindAddress != null ? bindAddress.getHostAddress() : "127.0.0.1";
    }

    public void setBindAddress(String host) throws UnknownHostException {
        // If host is null or empty use "any"-address
        bindAddress = (host != null && host.length() > 0) ? InetAddress.getByName("jboss.bind.address".equals(host) ? System.getProperty("jboss.bind.address", "127.0.0.1") : host) : null;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    public boolean getEnableTcpNoDelay() {
        return enableTcpNoDelay;
    }

    public void setEnableTcpNoDelay(boolean enableTcpNoDelay) {
        this.enableTcpNoDelay = enableTcpNoDelay;
    }

    public void setServerSocketFactory(String name) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class ssfClass = loader.loadClass(name);
        serverSocketFactory = (ServerSocketFactory) ssfClass.newInstance();
    }

    public String getServerSocketFactory() {
        return serverSocketFactory != null ? serverSocketFactory.getClass().getName() : null;
    }

    public String getCommand(String name) {
        return commandMap.get(name);
    }

    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
        commandMap.clear();
        log.log(Level.FINE, "initializing commands '" + commands + "'");
        if (commands != null) {
            for (String command : commands.split(";")) {
                command = command.trim();
                if (command.length() > 0) {
                    String sourceName = "/META-INF/commands/" + command;
                    log.log(Level.FINE, "loading command '" + command + "' from " + sourceName);
                    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(sourceName);
                    if (inputStream != null) {
                        try {
                            String code = StringUtil.getFromInputStream(inputStream);
                            commandMap.put(command, code);
                        } catch (IOException e) {
                            log.log(Level.SEVERE, "could not load command '" + command + "' from " + sourceName, e);
                        }
                    } else {
                        log.log(Level.SEVERE, "could not open resource " + sourceName);
                    }
                }
            }
        }
    }

    public String execute(String command, String[] args) throws Exception {
        String commandCode = getCommand(command);
        MBeanInvoker mbeanInvoker = new MBeanInvoker();
        CommandInvoker commandInvoker = new CommandInvoker(commandCode, mbeanInvoker);
        NrpeReturnValue returnValue = commandInvoker.invoke(args);
        return returnValue.toString();
    }
}
