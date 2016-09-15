package de.ivz.plm.util.option;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OptionParser - Ein abstrakter Parser zur Handhabung von Parametern und Optionen
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public abstract class OptionParser {

    protected static final Logger log = Logger.getLogger(OptionParser.class.getName());

    /**
     * Prüft die Existenz einer benannten Option
     * @param name der Name der Option
     * @return Status der Existenz
     */
    public abstract boolean hasOption(String name);

    /**
     * Gibt den repräsentierten Wert einer benannten Option zurück
     * @param name der Name der Option
     * @return der repräsentierte Wert
     */
    public abstract String getOption(String name);

    /**
     * Gibt eine Map aller Optionen mit deren repräsentierten Werten zurück
     * @return die Map
     */
    public abstract Map<String, String> getOptions();

    /**
     * Gibt eine benannte Option als String aus
     * @param name der Name der Option
     * @param defaultValue der Standardwert der Option, falls nicht existent bzw. ein Fehler auftritt
     * @return der Wert der Option als String
     */
    public String getStringOption(String name, String defaultValue) {
        return hasOption(name) ? String.valueOf(getOption(name)) : defaultValue;
    }

    /**
     * Gibt eine benannte Option als Integer aus
     * @param name der Name der Option
     * @param defaultValue der Standardwert der Option, falls nicht existent bzw. ein Fehler auftritt
     * @return der Wert der Option als Integer
     */
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

    /**
     * Gibt eine benannte Option als Long aus
     * @param name der Name der Option
     * @param defaultValue der Standardwert der Option, falls nicht existent bzw. ein Fehler auftritt
     * @return der Wert der Option als Long
     */
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

    /**
     * Gibt eine benannte Option als Double aus
     * @param name der Name der Option
     * @param defaultValue der Standardwert der Option, falls nicht existent bzw. ein Fehler auftritt
     * @return der Wert der Option als Double
     */
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

    /**
     * Gibt eine benannte Option als Boolean aus
     * @param name der Name der Option
     * @param defaultValue der Standardwert der Option, falls nicht existent bzw. ein Fehler auftritt
     * @return der Wert der Option als Boolean
     */
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
