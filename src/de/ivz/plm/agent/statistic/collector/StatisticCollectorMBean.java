package de.ivz.plm.agent.statistic.collector;

import java.util.Map;

/**
 * StatisticCollector als Managed Bean (Management Interface)
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public interface StatisticCollectorMBean {

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
     * Gibt den Intervall an, in dem Ausgaben der Statistiken gemacht werden sollen
     * @return das Intervall
     */
    public Long getInterval();

    /**
     * Setzt den Intervall (interval), in dem Ausgaben der Statistiken gemacht werden sollen
     * @param interval der gesetzt werden soll
     */
    public void setInterval(Long interval);

    /**
     * Gibt das Intervall an, in dem jeweils vollständige Ausgaben der Statistiken gemacht werden sollen
     * @return das Intervall
     */
    public Long getFullevery();

    /**
     * Setzt den Intervall (fullevery), in dem jeweils vollständige Ausgaben der Statistiken gemacht werden sollen
     * @param fullevery der gesetzt werden soll
     */
    public void setFullevery(Long fullevery);

    /**
     * Gibt an, ob bereits während der Instrumentierung eine Registrierung der Statistiken erfolgen soll
     * @return den Status
     */
    public boolean isFullreg();

    /**
     * Setzt den Status (fullreg), ob bereits während der Instrumentierung eine Registrierung der Statistiken erfolgen soll
     * @param fullreg der gesetzt werden soll
     */
    public void setFullreg(boolean fullreg);

    /**
     * Gibt an, ob bei der Ausgabe nur veränderte Daten berücksichtigt werden sollen
     * @return den Status
     */
    public boolean isCompact();

    /**
     * Setzt den Status (compact), ob bei der Ausgabe nur veränderte Daten berücksichtigt werden sollen
     * @param compact der gesetzt werden soll
     */
    public void setCompact(boolean compact);

    /**
     * Gibt den Namen (logger) des Loggers zurück, welcher zur Ausgabe der Statistiken verwendet werden soll
     * @return Name des Logger
     */
    public String getLogger();

    /**
     * Setzt den Namen des Logger, welcher zur Ausgabe der Statistiken verwendet werden soll
     * @param logger der gesetzt werden soll
     */
    public void setLogger(String logger);

    /**
     * Gibt eine in HTML formatierte Liste der Statistiken aus
     * @return die Tabelle
     */
    public String getHtml();

    /**
     *
     * @return Map mit den Statistiken
     */
    public Map<String, InvocationStatistics> getStatistics();
}
