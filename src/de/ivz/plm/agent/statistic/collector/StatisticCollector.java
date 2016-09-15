package de.ivz.plm.agent.statistic.collector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StatisticCollector als Managed Bean (Bean Implementation)
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class StatisticCollector extends TimerTask implements StatisticCollectorMBean {

    private static final Logger log = Logger.getLogger(StatisticCollector.class.getName());
    private static final Map<String, InvocationStatistics> classInvocationStatistics = new ConcurrentHashMap<String, InvocationStatistics>();

    private Timer timer;
    private Long interval;
    private Long fullevery;
    private String logger;
    private static boolean compact;
    private static boolean fullreg;

    private Long counter;

    @Override
    public void create() throws Exception {
        log.fine("create()");
        logger = null;
        interval = -1l;
        fullevery = -1l;
        counter = -1l;
    }

    @Override
    public void start() throws Exception {
        log.fine("start()");
        if (interval > 0) {
            try {
                timer = new Timer();
                timer.scheduleAtFixedRate(this, interval * 1000, interval * 1000);
            } catch (Exception e) {
                log.log(Level.WARNING, "could not start timer", e);
            }
        }
    }

    @Override
    public void stop() {
        log.fine("stop()");
        if (timer != null) {
            try {
                timer.cancel();
                timer = null;
            } catch (Exception e) {
                log.log(Level.WARNING, "could not cancel timer", e);
            }
        }
    }

    @Override
    public void destroy() {
        log.fine("destroy()");
        logger = null;
        interval = -1l;
        fullevery = -1l;
        counter = -1l;
    }

    @Override
    public String getLogger() {
        log.fine("getLogger()");
        return logger;
    }

    @Override
    public void setLogger(String logger) {
        log.fine("setLogger(" + logger + ")");
        this.logger = logger != null ? logger.trim() : "";
    }

    @Override
    public Long getInterval() {
        log.fine("getInterval()");
        return interval;
    }

    @Override
    public void setInterval(Long interval) {
        log.fine("setInterval(" + interval + ")");
        this.interval = interval != null ? interval : -1l;
    }

    @Override
    public Long getFullevery() {
        log.fine("getFullevery()");
        return fullevery;
    }

    @Override
    public void setFullevery(Long fullevery) {
        log.fine("setFullevery(" + fullevery + ")");
        this.fullevery = fullevery;
    }

    @Override
    public boolean isFullreg() {
        log.fine("isFullreg()");
        return fullreg;
    }

    @Override
    public void setFullreg(boolean fullreg) {
        log.fine("setFullreg(" + fullreg + ")");
        StatisticCollector.fullreg = fullreg;
    }

    @Override
    public boolean isCompact() {
        log.fine("isCompact()");
        return compact();
    }

    @Override
    public void setCompact(boolean compact) {
        log.fine("setCompact(" + compact + ")");
        StatisticCollector.compact = compact;
    }

    @Override
    public String getHtml() {
        log.fine("getHtml()");
        StringBuilder html = new StringBuilder();
        html.append("<table>");
        for (Map.Entry<String, InvocationStatistics> classStatistic : classInvocationStatistics.entrySet()) {
            html.append("<tr><th colspan=\"6\" style=\"text-align:left\">");
            html.append(classStatistic.getKey());
            html.append("&nbsp;&nbsp;<span style=\"font-weight:normal\">(MaxConcurrentCalls:");
            html.append(classStatistic.getValue().maxConcurrentCalls);
            html.append(", LastResetTime:");
            html.append(classStatistic.getValue().lastResetTime);
            html.append("</span></th></tr>");
            html.append("<tr><th style=\"text-align:left\">Method</th><th>Count</th><th>TotalTime</th><th>MinTime</th><th>MaxTime</th><th>AvgTime</th></tr>");
            for (Map.Entry<String, InvocationStatistics.TimeStatistic> methodStatistic : classStatistic.getValue().methodStatistics.entrySet()) {
                InvocationStatistics.TimeStatistic timeStatistic = methodStatistic.getValue();
                String[] pair = methodStatistic.getKey().split(" ");
                html.append("<tr><td>");
                html.append(pair[0]);
                html.append("</td><td>");
                html.append(timeStatistic.count);
                html.append("</td><td>");
                html.append(timeStatistic.totalTime);
                html.append("</td><td>");
                html.append(timeStatistic.minTime);
                html.append("</td><td>");
                html.append(timeStatistic.maxTime);
                html.append("</td><td>");
                html.append(timeStatistic.count > 0l ? timeStatistic.totalTime / timeStatistic.count : 0l);
                html.append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    @Override
    public Map<String, InvocationStatistics> getStatistics() {
        log.fine("getStatistics()");
        return StatisticCollector.classInvocationStatistics;
    }

    @Override
    /**
     * Scheduler-Methode
     * @see TimerTask
     */
    public void run() {
        log.fine("run()");
        long duration = System.currentTimeMillis();
        List<String> jsonStrings = new LinkedList<String>();

        // synchronized access to map of statistics
        synchronized (classInvocationStatistics) {
            // iterate over all entries
            for (Map.Entry<String, InvocationStatistics> classStatistic : classInvocationStatistics.entrySet()) {
                // stringbuilder for json string
                StringBuilder json = new StringBuilder();
                // flag to check if methods where appended - compact json
                boolean methodAppended = false;

                // add statistics to json string
                json.append("{\"class\":\"");
                json.append(classStatistic.getKey());
                json.append("\", \"lastResetTime\":");
                json.append(classStatistic.getValue().lastResetTime);
                json.append(", \"maxConcurrentCalls\":");
                json.append(classStatistic.getValue().maxConcurrentCalls);
                json.append(", \"invocations\":[");

                // itarate over all timed statistics of an entry
                for (Map.Entry<String, InvocationStatistics.TimeStatistic> methodStatistic : classStatistic.getValue().methodStatistics.entrySet()) {
                    InvocationStatistics.TimeStatistic timeStatistic = methodStatistic.getValue();
                    // compact-mode: only append data to json on first run, full run or if this method was called
                    if (!compact || counter < 0 || counter >= fullevery || timeStatistic.count > 0) {
                        String[] pair = methodStatistic.getKey().split(" ");
                        json.append("\n\t");
                        json.append("{\"method\":\"");
                        json.append(pair[0]);
                        json.append("\", \"signature\":\"");
                        json.append(pair.length > 1 ? pair[1] : "");
                        json.append("\", \"count\":");
                        json.append(timeStatistic.count);
                        json.append(", \"totalTime\":");
                        json.append(timeStatistic.totalTime);
                        json.append(", \"minTime\":");
                        json.append(timeStatistic.minTime);
                        json.append(", \"maxTime\":");
                        json.append(timeStatistic.maxTime);
                        json.append(", \"avgTime\":");
                        json.append(timeStatistic.count > 0l ? timeStatistic.totalTime / timeStatistic.count : 0l);
                        json.append("},");
                        methodAppended = true;
                    }
                }

                // remove last comma from json string
                json.deleteCharAt(json.length() - 1);
                // and close it in a regular way
                json.append("]}");

                //  compact-mode: add json string to list only if methods where appended
                if (!compact || methodAppended) {
                    jsonStrings.add(json.toString());
                }
            }
            reset();
        }

        // output all json strings to logger
        for (String jsonString : jsonStrings) {
            if (logger != null && logger.length() > 0) {
                Logger.getLogger(logger).info(jsonString);
            } else {
                log.info(jsonString);
            }
        }

        counter = counter < 0 ? 1 : counter + 1;
        duration = System.currentTimeMillis() - duration;
        log.info("InvocationStatistics collected in " + duration + "ms");
    }


    /* Die statischen Methoden sind nicht für das Trace-Logging (FINE) präpariert! */
    /* Hier würde ein erheblicher Overhead beim Aufruf während der Instrumentierung und dem Laufzeit-Tracing enstehen! */

    /**
     * Gibt die Statistiken für eine bestimmte Klasse zurück
     * @param className der vollqualifizierte Name der Klasse
     * @return Statistiken der Klasse
     */
    private static InvocationStatistics get(String className) {
        InvocationStatistics invocationStatistics = classInvocationStatistics.get(className);
        if (invocationStatistics == null) {
            invocationStatistics = new InvocationStatistics();
            classInvocationStatistics.put(className, invocationStatistics);
        }
        return invocationStatistics;
    }

    /**
     * Signalisiert den Beginn eines Methodenaufrufs für eine bestimmte Klasse
     * @param className der vollqualifizierte Name der Klasse
     * @see InvocationStatistics
     */
    public static void in(String className) {
        get(className).callIn();
    }

    /**
     * Signalisiert das Ende eines Methodenaufrufs für eine bestimmte Klasse
     * @param className der vollqualifizierte Name der Klasse
     * @see InvocationStatistics
     */
    public static void out(String className) {
        get(className).callOut();
    }

    /**
     * Aktualisiert die TimeStatistic der angegebenen Methode für eine bestimmte Klasse
     * @param className der vollqualifizierte Name der Klasse
     * @param methodInfo die Signatur der Methode, welche aktualisert werden soll
     * @param timeValue die benötigte Zeit für die Ausführung der Methode in Millisekunden
     * @see InvocationStatistics
     */
    public static void update(String className, String methodInfo, long timeValue) {
        get(className).update(methodInfo, timeValue);
    }

    /**
     * Setzt den Zustand und die Werte aller Statistiken zurück
     * @see InvocationStatistics
     */
    public static void reset() {
        synchronized (classInvocationStatistics) {
            for (InvocationStatistics invocationStatistics : classInvocationStatistics.values()) {
                invocationStatistics.reset();
            }
        }
    }

    /**
     * Zeigt an, ob der Compact-Modus aktiviert ist
     * @return Status des Compact-Modus
     * @see StatisticCollectorMBean
     */
    public static boolean compact() {
        return StatisticCollector.compact;
    }

    /**
     * Zeigt an, ob bereits während der Instrumentierung eine Registrierung der Statistiken erfolgen soll
     * @return Status des Fullref-Modus
     * @see StatisticCollectorMBean
     */
    public static boolean fullreg() {
        return StatisticCollector.fullreg;
    }
}
