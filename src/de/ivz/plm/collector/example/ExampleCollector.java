package de.ivz.plm.collector.example;

import de.ivz.plm.collector.Collector;

import java.util.logging.Level;

public class ExampleCollector extends Collector {

    private String parameter;

    private ExampleCollector(String[] args) {
        super(args);
        // initialize options
        parameter = optionParser.getStringOption("parameter", "example");
        log.log(Level.INFO, "parameter=" + parameter);
        // start timer
        start();
    }

    @Override
    public void collect() {
        // collect something
        log.log(Level.INFO, "example operation");
        // log result of collection to logger
        logger().info("log result of example collection");
    }

    public static void main(String[] args) {
        new ExampleCollector(args);
    }
}
