package de.ivz.plm.util.option;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringOptionParser extends OptionParser {

    Map<String, String> options;

    public StringOptionParser(String line) {
        options = new LinkedHashMap<>();
        if (line != null && line.length() > 0) {
            for (String token : line.split(",")) {
                String[] option = token.split("=");
                options.put(option[0].trim(), option.length > 1 ? option[1].trim() : "");
            }
        }
    }

    @Override
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    @Override
    public String getOption(String name) {
        return options.get(name);
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }
}
