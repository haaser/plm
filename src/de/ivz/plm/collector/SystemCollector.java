package de.ivz.plm.collector;

import com.sun.management.OperatingSystemMXBean;
import org.json.simple.JSONValue;

import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class SystemCollector extends Collector {

    public SystemCollector(String[] args) {
        super(args);
        // start timer
        start();
    }

    @Override
    public void collect() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        Map<String, Object> metric = new LinkedHashMap<>();
        Map<String, Double> memory = new LinkedHashMap<>();
        Map<String, Double> swap = new LinkedHashMap<>();
        Map<String, Double> cpu = new LinkedHashMap<>();

        metric.put("memory", memory);
        metric.put("swap", swap);
        metric.put("cpu", cpu);

        memory.put("total", (double) operatingSystemMXBean.getTotalPhysicalMemorySize());
        memory.put("free", (double) operatingSystemMXBean.getFreePhysicalMemorySize());
        memory.put("used", (double) (operatingSystemMXBean.getTotalPhysicalMemorySize() - operatingSystemMXBean.getFreePhysicalMemorySize()));

        swap.put("total", (double) operatingSystemMXBean.getTotalSwapSpaceSize());
        swap.put("free", (double) operatingSystemMXBean.getFreeSwapSpaceSize());
        swap.put("used", (double) (operatingSystemMXBean.getTotalSwapSpaceSize() - operatingSystemMXBean.getFreeSwapSpaceSize()));

        cpu.put("total", (double) operatingSystemMXBean.getAvailableProcessors());
        cpu.put("free", (double) operatingSystemMXBean.getAvailableProcessors() - operatingSystemMXBean.getSystemCpuLoad());
        cpu.put("used", operatingSystemMXBean.getSystemCpuLoad());

        String jsonString = JSONValue.toJSONString(metric);

        System.out.format("%-20s %12s %12s %12s%n", "name", "total", "used", "avail");
        for (FileStore store : FileSystems.getDefault().getFileStores()) {
            try {
                String name = store.name();
                String type = store.type();
                long total = store.getTotalSpace() / 1024 / 1024;
                long used = (store.getTotalSpace() - store.getUnallocatedSpace()) / 1024 / 1024;
                long avail = store.getUsableSpace() / 1024 / 1024;
                System.out.format("%-20s %12d %12d %12d%n", name, total, used, avail);
            } catch (Exception e) {
                log.log(Level.SEVERE, "an error occurred while analyzing the filesystem", e);
            }

        }

        logger().info(jsonString);
    }

    public static void main(String[] args) {
        new SystemCollector(args);
    }
}
