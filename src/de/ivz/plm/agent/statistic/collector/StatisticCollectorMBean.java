package de.ivz.plm.agent.statistic.collector;

import de.ivz.plm.agent.statistic.collector.InvocationStatistics;

import java.util.Map;

public interface StatisticCollectorMBean {

    public void create() throws Exception;
    public void start() throws Exception;
    public void stop();
    public void destroy();

    public Long getInterval();
    public void setInterval(Long interval);

    public Long getFullevery();
    public void setFullevery(Long fullevery);

    public boolean isFullreg();
    public void setFullreg(boolean fullreg);

    public boolean isCompact();
    public void setCompact(boolean compact);

    public String getLogger();
    public void setLogger(String logger);

    public String getHtml();
    public Map<String, InvocationStatistics> getStatistics();
}
