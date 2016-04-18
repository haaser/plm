package de.ivz.plm.collector;

import org.json.simple.JSONValue;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

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

    private JdbcCollector(String[] args) {
        super(args);
        // initialize options
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
        // prepare collector
        if (driver != null && url != null && username != null && password != null && request != null) {
            try {
                // load class of driver
                Class.forName(driver);
                // start timer
                start();
            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, "unable to load driver class", e);
            }
        } else {
            log.log(Level.SEVERE, "missing options: driver, url, username, password or request!");
        }
    }

    @Override
    public void collect() {
        if (stamp == null) {
            stamp = System.currentTimeMillis() / 1000 - interval / 1000;
        }
        for (Object obj : request.entrySet()) {
            Map.Entry query = (Map.Entry) obj;
            request(String.valueOf(query.getKey()), String.valueOf(query.getValue()), stamp);
        }
        stamp = (System.currentTimeMillis() / 1000);
    }

    private void request(String context, String query, long stamp) {
        log.log(Level.FINE, "request: context=" + context + ", query=" + query);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.prepareStatement(query);
            if (query.contains("?")) {
                statement.setTimestamp(1, new Timestamp(stamp));
            }
            result = statement.executeQuery();
            HashMap<String, Object> metric = new LinkedHashMap<String, Object>();
            while (result.next()) {
                metric.clear();
                metric.put("context", context);
                for (int i = 0; i < result.getMetaData().getColumnCount(); i++) {
                    metric.put(result.getMetaData().getColumnName(i + 1).toLowerCase(), result.getObject(i + 1));
                }
                String jsonString = JSONValue.toJSONString(metric);
                logger().info(jsonString);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "an error occurred while querying the database\ncontext: " + context + "\nquery: " + query, e);
        } finally {
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

    public static void main(String[] args) {
        new JdbcCollector(args);
    }
}
