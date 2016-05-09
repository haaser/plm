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

public class LivestatusCollector extends Collector {

    public static final String OPTION_HOST     = "host";
    public static final String OPTION_PORT     = "port";
    public static final String OPTION_REQUEST  = "request";

    private String host;
    private Integer port;
    private Map request;
    private Long stamp;

    public LivestatusCollector(String[] args) {
        super(args);
        // initialize options
        host = optionParser.getStringOption(OPTION_HOST, null);
        log.log(Level.INFO, OPTION_HOST + "=" + host);
        port = optionParser.getIntegerOption(OPTION_PORT, null);
        log.log(Level.INFO, OPTION_PORT + "=" + port);
        request = (Map)optionParser.getObject(OPTION_REQUEST);
        log.log(Level.INFO, OPTION_REQUEST + "=" + request);
        // prepare collector
        if (host != null && port != null && request != null) {
            // start timer
            start();
        } else {
            log.log(Level.SEVERE, "missing options: host, port or request!");
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

    private void request(String context, String columns, long stamp) {
        log.log(Level.FINE, "request: context=" + context + ", columns=" + columns);
        Socket socket = null;
        try {
            // create socket and prepare input/output-streams
            socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), false);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send livestatus-request
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

            // read livestatus-response
            String buffer;
            StringBuilder response = new StringBuilder();
            while ((buffer = in.readLine()) != null) {
                response.append(buffer);
            }
            socket.close();

            // decode data to json-object
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
                        String jsonString = JSONValue.toJSONString(metric);
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

    public static void main(String[] args) {
        new LivestatusCollector(args);
    }
}
