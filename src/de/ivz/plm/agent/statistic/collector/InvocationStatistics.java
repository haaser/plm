package de.ivz.plm.agent.statistic.collector;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InvocationStatistics - Zum Sammeln von Laufzeitstatistiken einer Klasse und dessen Methoden
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class InvocationStatistics implements Serializable {

    /** Eine HashMap<String, TimeStatistic> für die Statistiken der Methodenaufrufe */
    protected final Map<String, TimeStatistic> methodStatistics;
    protected long lastResetTime;
    protected long concurrentCalls;
    protected long maxConcurrentCalls;


    /**
    * Konstruktor der Klasse, welcher gleichzeitig die Werte initialisiert
    */
    public InvocationStatistics() {
        methodStatistics = new ConcurrentHashMap<String, TimeStatistic>();
        lastResetTime = System.currentTimeMillis();
        concurrentCalls = 0l;
        maxConcurrentCalls = 0l;
    }

    /**
     * Aktualisiert die TimeStatistic der angegebenen Methode
     *
     * @param methodSignature   die Signatur der Methode, welche aktualisert werden soll
     * @param timeValue         die benötigte Zeit für die Ausführung der Methode in Millisekunden
     */
    public void update(String methodSignature, long timeValue) {
        TimeStatistic timeStatistic = methodStatistics.get(methodSignature);
        if (timeStatistic == null) {
            timeStatistic = new TimeStatistic();
            methodStatistics.put(methodSignature, timeStatistic);
        }
        if (timeValue > -1l) {
            timeStatistic.count++;
            timeStatistic.totalTime += timeValue;
            if (timeValue < timeStatistic.minTime) {
                timeStatistic.minTime = timeValue;
            }
            if (timeValue > timeStatistic.maxTime) {
                timeStatistic.maxTime = timeValue;
            }
        }
    }

    /*
    * Signalisiert den Beginn eines Methodenaufrufs
    */
    public synchronized void callIn() {
        concurrentCalls++;
        if (concurrentCalls > maxConcurrentCalls) {
            maxConcurrentCalls = concurrentCalls;
        }
    }

    /*
    * Signalisiert das Ende eines Methodenaufrufs
    */
    public synchronized void callOut() {
        concurrentCalls--;
    }

    /**
    * Setzt den Zustand und die Werte aller Statistiken zurück
    */
    public void reset() {
        synchronized (methodStatistics) {
            lastResetTime = System.currentTimeMillis();
            concurrentCalls = 0l;
            maxConcurrentCalls = 0l;
            for (TimeStatistic timeStatistic : methodStatistics.values()) {
                timeStatistic.count = 0l;
                timeStatistic.minTime = 0l;
                timeStatistic.maxTime = 0l;
                timeStatistic.totalTime = 0l;
            }
        }
    }

    /**
     * TimeStatistic - Innere Klasse zur gekapselten Abbildung der Werte
     *
     * @author Ryczard Haase
     * @version 1.0
     */
    public class TimeStatistic implements Serializable {

        long count = 0l;
        long minTime = 0l;
        long maxTime = 0l;
        long totalTime = 0l;

    }
}
