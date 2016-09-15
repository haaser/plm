package de.ivz.plm.collector;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * JdbcCollector - Zum Sammeln von Informationen aus einen Nagios-System über dess Schnittselle "MK-Livestatus"
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class LivestatusCollector extends Collector {

    public static final String OPTION_HOST     = "host";
    public static final String OPTION_PORT     = "port";
    public static final String OPTION_REQUEST  = "request";

    private String host;
    private Integer port;
    private Map request;
    private Long stamp;

    /**
     * @see de.ivz.plm.collector.Collector
     */
    public LivestatusCollector(String[] args) {
        super(args);
        // Initialisiere die Optionen
        host = optionParser.getStringOption(OPTION_HOST, null);
        log.log(Level.INFO, OPTION_HOST + "=" + host);
        port = optionParser.getIntegerOption(OPTION_PORT, null);
        log.log(Level.INFO, OPTION_PORT + "=" + port);
        request = (Map)optionParser.getObject(OPTION_REQUEST);
        log.log(Level.INFO, OPTION_REQUEST + "=" + request);
        // Starte den Kollektor
        if (host != null && port != null && request != null) {
            // TimerTask.start()
            start();
        } else {
            log.log(Level.SEVERE, "missing options: host, port or request!");
        }
    }

    /**
     * @see de.ivz.plm.collector.Collector
     */
    @Override
    public void collect() {
        // Initialisiere den Zeitstempel
        if (stamp == null) {
            stamp = System.currentTimeMillis() / 1000 - interval / 1000;
        }
        // Setze alle konfigurierten Anfragen nacheinander ab
        for (Object obj : request.entrySet()) {
            // Wandle die Anfrage aus dem Map.Entry
            Map.Entry query = (Map.Entry) obj;
            // setzte de Anfrage ab
            request(String.valueOf(query.getKey()), String.valueOf(query.getValue()), stamp);
        }
        stamp = (System.currentTimeMillis() / 1000);
    }

    /**
     * Setzt eine Anfrage an die Schnittstelle "MK-Livstatus" des Nagios-Systems ab und wertet die Antwort aus
     * @param context Angabe des Kontexts
     * @param columns Angabe der Spalten
     * @param stamp Angabe des Zeitstempels der letzten Anfrage (zur Delta-Berechnung)
     */
    private void request(String context, String columns, long stamp) {
        log.log(Level.FINE, "request: context=" + context + ", columns=" + columns);
        Socket socket = null;
        try {
            // Erstelle/Öffne einen Socket und bereite die Input-/OutputStreams vor
            socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), false);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Versende die Livestatus-Anfrage
            out.write("GET " + context + "\n");
            out.write("Filter: last_check >= " + stamp + "\n");

            String[] column = columns != null ? columns.split("\\s+") : null;

            if (column != null && column.length > 0) {
                out.write("Columns:");
                for (int i = 0; i < column.length; i++) {
                    out.write(" " + column[i]);
                }
                out.write("\n");
            }
            out.write("OutputFormat: json\n\n");
            out.flush();

            // Lese die Daten aus Livestatus-Anwort ein
            String buffer;
            StringBuilder response = new StringBuilder();
            while ((buffer = in.readLine()) != null) {
                response.append(buffer);
            }
            socket.close();

            // Parse die Daten als JSON-Objekt
            JSONArray array = (JSONArray)JSONValue.parse(response.toString().replaceAll(",]", "]"));
            if (array != null) {
                log.info(array.size() + " lines received");
                HashMap<String, Object> metric = new LinkedHashMap<String, Object>();
                for (int i = 0; i < array.size(); i++) {

                    JSONArray entry = (JSONArray) array.get(i);
                    if (i == 0 && (columns == null || column.length == 0)) {
                        column = new String[entry.size()];
                        for (int j = 0; j < column.length; j++) {
                            column[j] = (String)entry.get(j);
                        }
                    } else {
                        metric.clear();
                        metric.put("context", context);
                        for (int j = 0; j < column.length; j++) {
                            metric.put(column[j], entry.get(j));
                        }

                        // Formatiere die Map als JSON-Struktur in einen String
                        String jsonString = JSONValue.toJSONString(metric);
                        // Gebe den JSON-String über den Logger aus
                        logger().info(jsonString);
                    }
                }
            } else {
                log.log(Level.SEVERE, "invalid data received: " + response.toString());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "something's gone wrong", e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Hauptmethode zum Starten
     * @param args Programm Optionen
     */
    public static void main(String[] args) {
        new LivestatusCollector(args);
    }
}
