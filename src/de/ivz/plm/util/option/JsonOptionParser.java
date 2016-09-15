package de.ivz.plm.util.option;

import org.json.simple.JSONValue;

import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JsonOptionParser - Ein Parser zur Handhabung von Optionen in einer JSON-Struktur
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class JsonOptionParser extends OptionParser {

    private Map options;

    /**
     * Konstruktor mit Angabe eines Readers
     * @param reader Reader zum Einlesen der JSON-Struktur
     */
    public JsonOptionParser(Reader reader) throws Exception {
        options = new HashMap();
        if (reader != null) {
            options = (Map) JSONValue.parseWithException(reader);
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
        return (options.get(name) instanceof String) ? (String)options.get(name) : null;
    }

    /**
     * @see de.ivz.plm.util.option.OptionParser
     */
    @Override
    public Map<String, String> getOptions() {
        Map<String, String> stringOption = new LinkedHashMap<String, String>();
        for (Object obj : options.keySet()) {
            stringOption.put(obj.toString(), getOption(obj.toString()));
        }
        return stringOption;
    }

    /**
     * Gibt eine benannte (komplexere) Option als Objekt aus
     * @param name der Name der Option
     * @return die Option als Objekt
     */
    public Object getObject(String name) {
        return options.get(name);
    }
}
