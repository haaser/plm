package de.ivz.plm.agent.statistic.collector;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class InvocationStatistics implements Serializable {

    /** A HashMap<String, TimeStatistic> of the method invocations */
    protected final Map<String, TimeStatistic> methodStatistics;
    protected long lastResetTime;
    protected long concurrentCalls;
    protected long maxConcurrentCalls;


    public InvocationStatistics() {
        methodStatistics = new ConcurrentHashMap<String, TimeStatistic>();
        lastResetTime = System.currentTimeMillis();
        concurrentCalls = 0l;
        maxConcurrentCalls = 0l;
    }

    /**
     * Update the TimeStatistic for the given method.
     *
     * @param methodSignature   the method signature to update the statistics for.
     * @param timeValue     the elapsed time in milliseconds for the invocation.
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

    public synchronized void callIn() {
        concurrentCalls++;
        if (concurrentCalls > maxConcurrentCalls) {
            maxConcurrentCalls = concurrentCalls;
        }
    }

    public synchronized void callOut() {
        concurrentCalls--;
    }

    /** Resets all current TimeStatistics. */
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

    public class TimeStatistic implements Serializable {

        long count = 0l;
        long minTime = 0l;
        long maxTime = 0l;
        long totalTime = 0l;

    }
}
