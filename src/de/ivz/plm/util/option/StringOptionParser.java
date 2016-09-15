package de.ivz.plm.util.option;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * StringOptionParser - Ein Parser zur Handhabung von Optionen in einem String
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class StringOptionParser extends OptionParser {

    Map<String, String> options;

    /**
     * Konstruktor mit Angabe des String mit den Optionen
     * @param line der String (die Optionen sind Kommna-separiert)
     */
    public StringOptionParser(String line) {
        options = new LinkedHashMap<String, String>();
        if (line != null && line.length() > 0) {
            for (String token : line.split(",")) {
                String[] option = token.split("=");
                options.put(option[0].trim(), option.length > 1 ? option[1].trim() : "");
            }
        }
    }

    /**
     * @see de.ivz.plm.util.option.OptionParser
     */
    @Override
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    /**
     * @see de.ivz.plm.util.option.OptionParser
     */
    @Override
    public String getOption(String name) {
        return options.get(name);
    }

    /**
     * @see de.ivz.plm.util.option.OptionParser
     */
    @Override
    public Map<String, String> getOptions() {
        return options;
    }
}
