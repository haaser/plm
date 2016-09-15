package de.ivz.plm.agent.nrpe.connector;

import java.net.UnknownHostException;

/**
 * NrpeConnector als Managed Bean (Management Interface)
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public interface NrpeConnectorMBean {

    /**
     * MBean-Lifecycle <i>CREATE</i>
     * @throws Exception wenn aufgetreten
     */
    public abstract void create() throws Exception;

    /**
     * MBean-Lifecycle <i>START</i>
     * @throws Exception wenn aufgetreten
     */
    public abstract void start() throws Exception;

    /**
     * MBean-Lifecycle <i>STOP</i>
     */
    public abstract void stop();

    /**
     * MBean-Lifecycle <i>DESTROY</i>
     */
    public abstract void destroy();

    /**
     * Zeigt an, ob der Connector gestartet ist und Anfragen beantworten kann
     * @return
     */
    public boolean isRunning();

    /**
     * Gibt die Adresse an, auf die der ServerSocket gebunden wird
     * @return die Adresse
     */
    public String getBindAddress();

    /**
     * Setzt die Adresse, auf die der ServerSocket gebunden werden soll
     * @param bindAddress die Adresse
     * @throws UnknownHostException wenn aufgetreten
     */
    public void setBindAddress(String bindAddress) throws UnknownHostException;

    /**
     * Gibt den Port an, auf den der ServerSocket gebunden wird
     * @return der Port
     */
    public int getBindPort();

    /**
     * Setzt den Port, auf den der ServerSocket gebunden werden soll
     * @param port der Port
     */
    public void setBindPort(int port);

    /**
     * Gibt den ReadTimeout an, der für das Einlesen von Nachrichten auf dem Socket eingestellt ist
     * @return der ReadTimeout
     */
    public int getReadTimeout();

    /**
     * Setzt das ReadTimeout, , der für das Einlesen von Nachrichten auf dem Socket eingestellt werden soll
     * @param timeout der ReadTimeout
     */
    public void setReadTimeout(int timeout);

    /**
     * Gibt an, ob Nagle-Algorithmus auf dem Socket aktiviert ist
     * @return der ReadTimeout
     */
    public boolean getEnableTcpNoDelay();

    /**
     * Dre-/Aktiviert den Nagle-Algorithmus auf dem Socket
     * @param enableTcpNoDelay der Status der Aktivierung
     */
    public void setEnableTcpNoDelay(boolean enableTcpNoDelay);

    /**
     * Gibt den Namen der SocketFactory an, welche zur Erzeugung der Sockets verwendet wird
     * @return der Name
     */
    public String getServerSocketFactory();

    /**
     * Setzt den Namen der SocketFactory, welche zur Erzeugung der Sockets verwendet werden soll
     * @param name die zu vewendende SocketFactory
     * @throws Exception
     */
    public void setServerSocketFactory(String name) throws Exception;

    /**
     * Gibt den Code für das enstprechnde Kommando zurück
     * @return das Kommando
     */
    public String getCommand(String commands);

    /**
     * Gibt alle verfügbaren Kommandos zurücks
     * @return die Kommandos
     */
    public String getCommands();

    /**
     * Setzt die Liste (komman-separiert), welche verfügbar sind
     * @param commands die Kommandos
     */
    public void setCommands(String commands);

    /**
     * Führt den Code eines Kommandos aus
     * @param name Kommando
     * @param args Parameter
     * @return Ausgabe vom Code
     * @throws Exception wen aufgetreten
     */
    public String execute(String name, String[] args) throws Exception;
}
