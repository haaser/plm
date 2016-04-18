package de.ivz.plm.util.option;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OptionParser {

    protected static final Logger log = Logger.getLogger(OptionParser.class.getName());

    public abstract boolean hasOption(String name);

    public abstract String getOption(String name);

    public abstract Map<String, String> getOptions();

    public String getStringOption(String name, String defaultValue) {
        return hasOption(name) ? String.valueOf(getOption(name)) : defaultValue;
    }

    public Integer getIntegerOption(String name, Integer defaultValue) {
        Integer value = defaultValue;
        if (hasOption(name)) {
            try {
                value = Integer.parseInt(String.valueOf(getOption(name)));
            } catch (NumberFormatException e) {
                log.log(Level.SEVERE, "could not parse option '" + name + "' with value '" + value + "' as integer", e);
            }
        }
        return value;
    }

    public Long getLongOption(String name, Long defaultValue) {
        Long value = defaultValue;
        if (hasOption(name)) {
            try {
                value = Long.parseLong(String.valueOf(getOption(name)));
            } catch (NumberFormatException e) {
                log.log(Level.SEVERE, "could not parse option '" + name + "' with value '" + value + "' as long", e);
            }
        }
        return value;
    }

    public Double getDoubleOption(String name, Double defaultValue) {
        Double value = defaultValue;
        if (hasOption(name)) {
            try {
                value = Double.parseDouble(String.valueOf(getOption(name)));
            } catch (NumberFormatException e) {
                log.log(Level.SEVERE, "could not parse option '" + name + "' with value '" + value + "' as double", e);
            }
        }
        return value;
    }

    public Boolean getBooleanOption(String name, Boolean defaultValue) {
        Boolean value = defaultValue;
        if (hasOption(name)) {
            try {
                value = Boolean.parseBoolean(String.valueOf(getOption(name)));
            } catch (NumberFormatException e) {
                log.log(Level.SEVERE, "could not parse option '" + name + "' with value '" + value + "' as boolean", e);
            }
        }
        return value;
    }

}
