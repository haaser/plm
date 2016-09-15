package de.ivz.plm.collector;

import org.json.simple.JSONValue;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * JdbcCollector - Zum Sammeln von Informationen aus einer Datenbank
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class JdbcCollector extends Collector {

    public static final String OPTION_DRIVER   = "driver";
    public static final String OPTION_URL      = "url";
    public static final String OPTION_USERNAME = "username";
    public static final String OPTION_PASSWORD = "password";
    public static final String OPTION_REQUEST  = "request";

    private String driver;
    private String url;
    private String username;
    private String password;
    private Map request;
    private Long stamp;

    /**
     * @see de.ivz.plm.collector.Collector
     */
    private JdbcCollector(String[] args) {
        super(args);
        // Initialisiere die Optionen
        driver = optionParser.getStringOption(OPTION_DRIVER, null);
        log.log(Level.INFO, OPTION_DRIVER + "=" + driver);
        url = optionParser.getStringOption(OPTION_URL, null);
        log.log(Level.INFO, OPTION_URL + "=" + url);
        username = optionParser.getStringOption(OPTION_USERNAME, null);
        log.log(Level.INFO, OPTION_USERNAME + "=" + username);
        password = optionParser.getStringOption(OPTION_PASSWORD, null);
        log.log(Level.INFO, OPTION_PASSWORD + "=" + password);
        request = (Map)optionParser.getObject(OPTION_REQUEST);
        log.log(Level.INFO, OPTION_REQUEST + "=" + request);
        // Starte den Kollektor
        if (driver != null && url != null && username != null && password != null && request != null) {
            try {
                // Lade den Treiber der Datenbank
                Class.forName(driver);
                // TimerTask.start()
                start();
            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, "unable to load driver class", e);
            }
        } else {
            log.log(Level.SEVERE, "missing options: driver, url, username, password or request!");
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
     * Setzt eine Anfrage an Datenbank ab und wertet die Antwort aus
     * @param context Angabe des Kontexts
     * @param query Angabe der SQL_Query
     * @param stamp Angabe des Zeitstempels der letzten Anfrage (zur Delta-Berechnung)
     */
    private void request(String context, String query, long stamp) {
        log.log(Level.FINE, "request: context=" + context + ", query=" + query);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            // Erstelle/Öffne eine Verbindung zur Datenbank
            connection = DriverManager.getConnection(url, username, password);
            // Erstelle eine PrepearedStatement
            statement = connection.prepareStatement(query);
            // Setzte bei Verfügbarkeit eines "?" den Zeitstempel ein
            if (query.contains("?")) {
                statement.setTimestamp(1, new Timestamp(stamp));
            }
            // Setzte die Anfrage ab
            result = statement.executeQuery();
            // Erzeuge eine Map für die Informationen
            HashMap<String, Object> metric = new LinkedHashMap<String, Object>();
            // Werte alle Zeilen der Antwort aus
            while (result.next()) {
                // Setze die map zurück
                metric.clear();
                // Setzte den verwendeten Kontext in die Map
                metric.put("context", context);
                // Übertrage alle Ergebnisse aus der Anwortzeile in die Map
                for (int i = 0; i < result.getMetaData().getColumnCount(); i++) {
                    metric.put(result.getMetaData().getColumnName(i + 1).toLowerCase(), result.getObject(i + 1));
                }
                // Formatiere die Map als JSON-Struktur in einen String
                String jsonString = JSONValue.toJSONString(metric);
                // Gebe den JSON-String über den Logger aus
                logger().info(jsonString);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "an error occurred while querying the database\ncontext: " + context + "\nquery: " + query, e);
        } finally {
            // Schließe alle möglichen DB-Objekte
            try {
                if (result != null && !result.isClosed()) {
                    result.close();
                }
            } catch (SQLException e) {
                // ignore
            }
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                // ignore
            }
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Hauptmethode zum Starten
     * @param args Programm Optionen
     */
    public static void main(String[] args) {
        new JdbcCollector(args);
    }
}
