package de.ivz.plm.agent.statistic.collector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


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
        logger = null;
        interval = -1l;
        fullevery = -1l;
        counter = -1l;
    }

    @Override
    public void start() throws Exception {
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
        logger = null;
        interval = -1l;
        fullevery = -1l;
        counter = -1l;
    }

    @Override
    public String getLogger() {
        return logger;
    }

    @Override
    public void setLogger(String logger) {
        this.logger = logger != null ? logger.trim() : "";
    }

    @Override
    public Long getInterval() {
        return interval;
    }

    @Override
    public void setInterval(Long interval) {
        this.interval = interval != null ? interval : -1l;
    }

    @Override
    public Long getFullevery() {
        return fullevery;
    }

    @Override
    public void setFullevery(Long fullevery) {
        this.fullevery = fullevery;
    }

    @Override
    public boolean isFullreg() {
        return fullreg;
    }

    @Override
    public void setFullreg(boolean fullreg) {
        StatisticCollector.fullreg = fullreg;
    }

    @Override
    public boolean isCompact() {
        return compact();
    }

    @Override
    public void setCompact(boolean compact) {
        StatisticCollector.compact = compact;
    }

    @Override
    public String getHtml() {
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
        return StatisticCollector.classInvocationStatistics;
    }

    @Override
    public void run() {
        long duration = System.currentTimeMillis();
        List<String> jsonStrings = new LinkedList<String>();

        synchronized (classInvocationStatistics) {
            for (Map.Entry<String, InvocationStatistics> classStatistic : classInvocationStatistics.entrySet()) {
                StringBuilder json = new StringBuilder();
                boolean methodAppended = false;

                json.append("{\"class\":\"");
                json.append(classStatistic.getKey());
                json.append("\", \"lastResetTime\":");
                json.append(classStatistic.getValue().lastResetTime);
                json.append(", \"maxConcurrentCalls\":");
                json.append(classStatistic.getValue().maxConcurrentCalls);
                json.append(", \"invocations\":[");

                for (Map.Entry<String, InvocationStatistics.TimeStatistic> methodStatistic : classStatistic.getValue().methodStatistics.entrySet()) {
                    InvocationStatistics.TimeStatistic timeStatistic = methodStatistic.getValue();
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

                json.deleteCharAt(json.length() - 1);
                json.append("]}");

                if (!compact || methodAppended) {
                    jsonStrings.add(json.toString());
                }
            }
            reset();
        }

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



    private static InvocationStatistics get(String className) {
        InvocationStatistics invocationStatistics = classInvocationStatistics.get(className);
        if (invocationStatistics == null) {
            invocationStatistics = new InvocationStatistics();
            classInvocationStatistics.put(className, invocationStatistics);
        }
        return invocationStatistics;
    }

    public static void in(String className) {
        get(className).callIn();
    }

    public static void out(String className) {
        get(className).callOut();
    }

    public static void update(String className, String methodInfo, long timeValue) {
        get(className).update(methodInfo, timeValue);
    }

    public static void reset() {
        synchronized (classInvocationStatistics) {
            for (InvocationStatistics invocationStatistics : classInvocationStatistics.values()) {
                invocationStatistics.reset();
            }
        }
    }

    public static boolean compact() {
        return StatisticCollector.compact;
    }

    public static boolean fullreg() {
        return StatisticCollector.fullreg;
    }
}
