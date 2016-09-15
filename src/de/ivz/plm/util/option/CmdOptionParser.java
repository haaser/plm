package de.ivz.plm.util.option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * CmdOptionParser - Ein Parser zur Handhabung von Optionen über die Kommandozeile <I>public static void main(String[] args)</I>
 *
 * inspired by http://stackoverflow.com/questions/7356979/java-beanshell-scripting-with-args-to-the-the-program
 *
 * @author Ryczard Haase
 * @version 1.0
 */

public class CmdOptionParser extends OptionParser {

    private List<String> specialOptions = new ArrayList<String>();
    private Map<String, String> options = new HashMap<String, String>();
    private List<String> arguments = new ArrayList<String>();

    /**
     * Konstruktor mit Angabe eines Arrays von Optionen aus z.B. <I>public static void main(String[] args)</I>
     * @param args String-Array mit den Optionen
     */
    public CmdOptionParser(String[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].charAt(0) == '-') {
                    if (args[i].charAt(1) == '-') {
                        String specialOpt = args[i].substring(2, args[i].length());
                        log.log(Level.FINE, "add special option " + specialOpt);
                        specialOptions.add(specialOpt);
                    } else if (args[i].charAt(1) != '-' && args[i].length() > 2) {
                        String name = args[i].substring(1);
                        String value = args[i + 1];
                        log.log(Level.FINE, "found extended option: " + name + " with value " + value);
                        options.put(name, value);
                        i++;
                    } else if (args[i].charAt(1) != '-' && args[i].length() == 2) {
                        String name = args[i].substring(1);
                        String value = args[i + 1];
                        log.log(Level.FINE, "found regular option: " + name + " with value " + value);
                        options.put(name, value);
                        i++;
                    } else if (args[i].length() <= 1) {
                        log.log(Level.WARNING, "improperly formed arg found: " + args[i]);
                    }
                } else {
                    String name = args[i];
                    log.log(Level.FINE, "add arg to argument list: " + name);
                    arguments.add(name);
                }
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

    /**
     * Prüft die benannte Option, ob diese eine spezielle Option ist ("--optionA")
     * @param name Name der Option
     * @return Existenz als spezielle Option
     */
    public boolean hasSpecialOption(String name) {
        return specialOptions.contains(name);
    }

    /**
     * Gibt eine Liste aller spezielle Optionen zurück ("--optionA --optionC")
     * @return Liste mit den Optionen
     */
    public List<String> getSpecialOptions() {
        return specialOptions;
    }

    /**
     * Prüft, ob es das angegeben Argument gibt
     * @param name Name des Arguments
     * @return Existenz des Arguments
     */
    public boolean hasArgument(String name) {
        return arguments.contains(name);
    }

    /**
     * Gibt eine Liste aller Argumente zurück ("-A -C")
     * @return Liste mit den Argumenten
     */
    public List<String> getArguments() {
        return arguments;
    }
}
