package de.ivz.plm.collector.example;

import de.ivz.plm.collector.Collector;

import java.util.logging.Level;

/**
 * ExampleCollector - Beispielhafter Kollektor zur Veranschaulichung der Funktionsweise
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class ExampleCollector extends Collector {

    private String parameter;

    /**
     * @see de.ivz.plm.collector.Collector
     */
    private ExampleCollector(String[] args) {
        super(args);
        // Initialisiere die Optionen
        parameter = optionParser.getStringOption("parameter", "example");
        log.log(Level.INFO, "parameter=" + parameter);
        // Starte den Kollektor
        // TimerTask.start()
        start();
    }

    /**
     * @see de.ivz.plm.collector.Collector
     */
    @Override
    public void collect() {
        // Sammle irgendwelche Daten
        log.log(Level.INFO, "example operation");
        // Gebe den Information über den Logger aus
        logger().info("log result of example collection");
    }

    /**
     * Hauptmethode zum Starten
     * @param args Programm Optionen
     */
    public static void main(String[] args) {
        new ExampleCollector(args);
    }
}
