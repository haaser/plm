package de.ivz.plm.collector;

import org.json.simple.JSONValue;

import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * SystemCollector - Zum Sammeln von Informationen über das zugrundeliegende System
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class SystemCollector extends Collector {

    /**
     * @see de.ivz.plm.collector.Collector
     */
    public SystemCollector(String[] args) {
        super(args);
        // Starte den Kolletor
        // TimerTask.start()
        start();
    }

    /**
     * @see de.ivz.plm.collector.Collector
     */
    @Override
    public void collect() {
        com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        Map<String, Object> metric = new LinkedHashMap<String, Object>();
        Map<String, Double> memory = new LinkedHashMap<String, Double>();
        Map<String, Double> swap = new LinkedHashMap<String, Double>();
        Map<String, Double> cpu = new LinkedHashMap<String, Double>();
        List<Map> storage = new LinkedList<Map>();

        // Erzeuge eine Map für die Informationen
        metric.put("memory", memory);
        metric.put("swap", swap);
        metric.put("cpu", cpu);
        metric.put("storage", storage);

        // Ermittle die Speicherauslastung des RAM
        memory.put("total", (double) operatingSystemMXBean.getTotalPhysicalMemorySize());
        memory.put("free", (double) operatingSystemMXBean.getFreePhysicalMemorySize());
        memory.put("used", (double) (operatingSystemMXBean.getTotalPhysicalMemorySize() - operatingSystemMXBean.getFreePhysicalMemorySize()));

        // Ermittle die Speicherauslastung des SWAP
        swap.put("total", (double) operatingSystemMXBean.getTotalSwapSpaceSize());
        swap.put("free", (double) operatingSystemMXBean.getFreeSwapSpaceSize());
        swap.put("used", (double) (operatingSystemMXBean.getTotalSwapSpaceSize() - operatingSystemMXBean.getFreeSwapSpaceSize()));

        // Ermittle die Prozessorauslastung
        cpu.put("total", (double) operatingSystemMXBean.getAvailableProcessors());
        cpu.put("free", (double) operatingSystemMXBean.getAvailableProcessors() - (operatingSystemMXBean.getSystemCpuLoad() < 0 ? 0 : operatingSystemMXBean.getSystemCpuLoad()));
        cpu.put("used", (operatingSystemMXBean.getSystemCpuLoad() < 0 ? 0 : operatingSystemMXBean.getSystemCpuLoad()));

        // Ermittle die Kapazitäten aller angebundenen Speichergeräte
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            try {
                // Überführe die Inforationen in eine Map
                Map<String, Object> fsMetric = new LinkedHashMap<String, Object>();
                fsMetric.put("name", store.name());
                fsMetric.put("type", store.type());
                fsMetric.put("total", store.getTotalSpace() / 1024l / 1024l);
                fsMetric.put("used", (store.getTotalSpace() - store.getUnallocatedSpace()) / 1024l / 1024l);
                fsMetric.put("avail", store.getUsableSpace() / 1024l / 1024l);
                storage.add(fsMetric);
            } catch (Exception e) {
                log.log(Level.SEVERE, "an error occurred while analyzing the filesystem", e);
            }

        }

        // Formatiere die Map als JSON-Struktur in einen String
        String jsonString = JSONValue.toJSONString(metric);
        // Gebe den JSON-String über den Logger aus
        logger().info(jsonString);
    }

    /**
     * Hauptmethode zum Starten
     * @param args Programm Optionen
     */
    public static void main(String[] args) {
        new SystemCollector(args);
    }
}
