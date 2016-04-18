package de.ivz.plm.util.option;

import org.json.simple.JSONValue;

import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonOptionParser extends OptionParser {

    private Map options;

    public JsonOptionParser(Reader reader) throws Exception {
        options = new HashMap();
        if (reader != null) {
            options = (Map) JSONValue.parseWithException(reader);
        }
    }

    @Override
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    @Override
    public String getOption(String name) {
        return (options.get(name) instanceof String) ? (String)options.get(name) : null;
    }

    @Override
    public Map<String, String> getOptions() {
        Map<String, String> stringOption = new LinkedHashMap<>();
        for (Object obj : options.keySet()) {
            stringOption.put(obj.toString(), getOption(obj.toString()));
        }
        return stringOption;
    }

    public Object getObject(String name) {
        return options.get(name);
    }
}
