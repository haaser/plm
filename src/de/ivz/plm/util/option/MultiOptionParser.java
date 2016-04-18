package de.ivz.plm.util.option;

import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class MultiOptionParser extends OptionParser {

    private CmdOptionParser cmdOptionParser;
    private JsonOptionParser jsonOptionParser;

    public MultiOptionParser(String[] args, String configName) throws Exception {
        cmdOptionParser = new CmdOptionParser(args);
        if (cmdOptionParser.hasOption(configName)) {
            jsonOptionParser = new JsonOptionParser(new FileReader(cmdOptionParser.getOption(configName)));
        }
    }

    @Override
    public boolean hasOption(String name) {
        return cmdOptionParser.hasOption(name) || jsonOptionParser.hasOption(name);
    }

    @Override
    public String getOption(String name) {
        return cmdOptionParser.hasOption(name) ? cmdOptionParser.getOption(name) : jsonOptionParser.getOption(name);
    }

    @Override
    public Map<String, String> getOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.putAll(jsonOptionParser.getOptions());
        options.putAll(cmdOptionParser.getOptions());
        return options;
    }

    public Object getObject(String name) {
        return !cmdOptionParser.hasOption(name) ? jsonOptionParser.getObject(name) : null;
    }

    public boolean hasArgument(String name) {
        return cmdOptionParser.hasArgument(name);
    }

    public boolean hasSpecialOption(String name) {
        return cmdOptionParser.hasSpecialOption(name);
    }
}
