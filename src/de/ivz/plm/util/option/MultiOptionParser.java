package de.ivz.plm.util.option;

import java.io.FileReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MultiOptionParser - Ein kumulierender Parser zur Handhabung verschiedender Quellen für Optionen
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class MultiOptionParser extends OptionParser {

    private CmdOptionParser cmdOptionParser;
    private JsonOptionParser jsonOptionParser;

    /**
     * Konstruktor mit Angabe eines Arrays von Optionen aus z.B. <I>public static void main(String[] args)</I> sowie dem Namen einer Option, welche den Pfad zu einer JSON-Datei angibt
     * @param args String-Array mit den Optionen
     * @param configName Namen der Option, welche den Pfad zu einer JSON-Datei angibt
     */
    public MultiOptionParser(String[] args, String configName) throws Exception {
        cmdOptionParser = new CmdOptionParser(args);
        if (cmdOptionParser.hasOption(configName)) {
            jsonOptionParser = new JsonOptionParser(new FileReader(cmdOptionParser.getOption(configName)));
        } else {
            jsonOptionParser = new JsonOptionParser(new StringReader("{}"));
        }
    }

    /**
     * @see de.ivz.plm.util.option.OptionParser
     */
    @Override
    public boolean hasOption(String name) {
        return cmdOptionParser.hasOption(name) || jsonOptionParser.hasOption(name);
    }

    /**
     * @see de.ivz.plm.util.option.OptionParser
     */
    @Override
    public String getOption(String name) {
        return cmdOptionParser.hasOption(name) ? cmdOptionParser.getOption(name) : jsonOptionParser.getOption(name);
    }

    /**
     * @see de.ivz.plm.util.option.OptionParser
     */
    @Override
    public Map<String, String> getOptions() {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.putAll(jsonOptionParser.getOptions());
        options.putAll(cmdOptionParser.getOptions());
        return options;
    }

    /**
     * @see de.ivz.plm.util.option.CmdOptionParser
     * @see de.ivz.plm.util.option.JsonOptionParser
     */
    public Object getObject(String name) {
        return !cmdOptionParser.hasOption(name) ? jsonOptionParser.getObject(name) : null;
    }

    /**
     * @see de.ivz.plm.util.option.CmdOptionParser
     */
    public boolean hasArgument(String name) {
        return cmdOptionParser.hasArgument(name);
    }

    /**
     * @see de.ivz.plm.util.option.CmdOptionParser
     */
    public boolean hasSpecialOption(String name) {
        return cmdOptionParser.hasSpecialOption(name);
    }
}
