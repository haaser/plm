<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TimeZone" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%!
    String url    = "http://plm.host.address:9200";
    String index  = "log";
    String type   = "log";
    String domain = System.getProperty("jboss.connect.address");
    String server = System.getProperty("jboss.bind.address");

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    SimpleDateFormat jsonDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // split String by ";" and return as array
    public String[] asArray(String values) {
        LinkedList<String> result = new LinkedList<String>();
        if (values != null && values.trim().length() > 0) {
            for (String value : values.split(";")) {
                value = value.trim();
                if (!value.equals("*") && !value.equals("%") && !result.contains(value)) {
                    result.add(value);
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    // concat elements of array with ";" an return as string
    public String asString(String[] array) {
        String result = "";
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                String value = array[i].trim();
                if (value.length() > 0) {
                    if (result.length() > 0) {
                        result += ";";
                    }
                    result += value;
                }
            }
        }
        return result;
    }

    public String asString(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    public Long asLong(String value, Long defaultValue) {
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) { }
        }
        return defaultValue;
    }

    public Long asDate(String value, Long defaultValue) {
        if (value != null) {
            try {
                return dateFormat.parse(value).getTime();
            } catch (ParseException e) { }
        }
        return defaultValue;
    }

    public String call(String url, String method, String content, String encoding) throws Exception {
        if (url != null) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) (new URL(url)).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestMethod(method);
                connection.setDoOutput(true);
                if (content != null) {
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Length", Long.toString(content.length()));
                    connection.getOutputStream().write(content.getBytes(encoding));
                    connection.getOutputStream().write("\r\n".getBytes(encoding));
                    connection.getOutputStream().close();
                }
                connection.connect();
                BufferedReader reader = null;
                String rex = "";
                try {
                    if (connection.getInputStream() != null) {
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    }
                } catch (IOException e) {
                    rex = e.getMessage();
                    if (connection.getErrorStream() != null) {
                        reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    }
                }
                if (reader == null) {
                    throw new Exception("unable to read response: reader is null: " + rex);
                }
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();
                return buffer.toString();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return null;
    }
%>
<%
    dateFormat.setTimeZone(TimeZone.getDefault());
    dateTimeFormat.setTimeZone(TimeZone.getDefault());
    jsonDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    // analyze query-inputs
    Long pagesize = asLong(request.getParameter("pagesize"), 100l);
    if (pagesize < 1) pagesize = 1l;
    if (pagesize > 10000) pagesize = 10000l;
    Long entries = 0l;
    Long pages = 1l;
    Long current = asLong(request.getParameter("page"), 1l);
    if (request.getParameter("prev") != null) {
        current--;
    }
    if (request.getParameter("next") != null) {
        current++;
    }
    current = current < 1 ? 1 : current;
    Boolean auto = request.getParameter("auto") != null;

    String range = request.getParameter("range");
    Long date = asDate(range, null);
    Long hours = asLong(range, 24l);
    String[] levels  = asArray(request.getParameter("level"));
    String logger = asString(request.getParameter("logger"), "").trim();
    String thread = asString(request.getParameter("thread"), "").trim();
    String message = asString(request.getParameter("message"), "").trim();


    // query-filtered-filter-list section
    List<Map> filtered_filter_bool_must = new ArrayList<Map>();
    Map<String, List> filtered_filter_bool = new HashMap<String, List>();
    filtered_filter_bool.put("must", filtered_filter_bool_must);
    Map<String, Map> filtered_filter = new HashMap<String, Map>();
    filtered_filter.put("bool", filtered_filter_bool);

    // query-filtered-query-list section
    List<Map> filtered_query_bool_must = new ArrayList<Map>();
    Map<String, List> filtered_query_bool = new HashMap<String, List>();
    filtered_query_bool.put("must", filtered_query_bool_must);
    Map<String, Map> filtered_query = new HashMap<String, Map>();
    filtered_query.put("bool", filtered_query_bool);

    // query-filtered
    Map<String, Map> filtered = new HashMap<String, Map>();
    filtered.put("filter", filtered_filter);
    filtered.put("query", filtered_query);

    // query
    HashMap<String, Object> query = new HashMap<String, Object>();
    query.put("filtered", filtered);

    // filtered fixed type
    Map<String, String> bool_must_type_term = new HashMap<String, String>();
    bool_must_type_term.put("type", type);
    Map<String, Map> bool_must_type = new HashMap<String, Map>();
    bool_must_type.put("term", bool_must_type_term);
    filtered_filter_bool_must.add(bool_must_type);

    // filtered fixed domain
    Map<String, String> bool_must_domain_term = new HashMap<String, String>();
    bool_must_domain_term.put("domain", domain);
    Map<String, Map> bool_must_domain = new HashMap<String, Map>();
    bool_must_domain.put("term", bool_must_domain_term);
    filtered_filter_bool_must.add(bool_must_domain);

    // filtered timestamp
    Map<String, String> bool_must_timestamp_range_values = new HashMap<String, String>();
    if (date != null) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        bool_must_timestamp_range_values.put("from", jsonDateTimeFormat.format(cal.getTime()));
        cal.add(Calendar.DATE, 1);
        bool_must_timestamp_range_values.put("to", jsonDateTimeFormat.format(cal.getTime()));
    } else {
        bool_must_timestamp_range_values.put("gte", "now-" + hours + "h");
    }
    Map<String, Map> bool_must_timestamp_range = new HashMap<String, Map>();
    bool_must_timestamp_range.put("@timestamp", bool_must_timestamp_range_values);
    Map<String, Map> bool_must_range = new HashMap<String, Map>();
    bool_must_range.put("range", bool_must_timestamp_range);
    filtered_filter_bool_must.add(bool_must_range);

    // filtered level
    if (levels.length > 0) {
        Map<String, List> bool_must_level_terms = new HashMap<String, List>();
        bool_must_level_terms.put("level", new LinkedList<String>());
        for (String level : levels) {
            bool_must_level_terms.get("level").add(level.toLowerCase());
        }
        Map<String, Map> bool_must_level = new HashMap<String, Map>();
        bool_must_level.put("terms", bool_must_level_terms);
        filtered_filter_bool_must.add(bool_must_level);
    }

    // queried logger
    if (logger.length() > 0) {
        List<String> querystring_logger_fields = new ArrayList<String>();
        querystring_logger_fields.add("logger");
        Map<String, Object> querystring_logger = new HashMap<String, Object>();
        querystring_logger.put("query", logger);
        querystring_logger.put("fields", querystring_logger_fields);
        Map<String, Map> bool_must_logger = new HashMap<String, Map>();
        bool_must_logger.put("query_string", querystring_logger);
        filtered_query_bool_must.add(bool_must_logger);
    }

    // queried thread
    if (thread.length() > 0) {
        List<String> querystring_thread_fields = new ArrayList<String>();
        querystring_thread_fields.add("thread");
        Map<String, Object> querystring_thread = new HashMap<String, Object>();
        querystring_thread.put("query", thread);
        querystring_thread.put("fields", querystring_thread_fields);
        Map<String, Map> bool_must_thread = new HashMap<String, Map>();
        bool_must_thread.put("query_string", querystring_thread);
        filtered_query_bool_must.add(bool_must_thread);
    }

    // queried message/throwable
    if (message.length() > 0) {
        List<String> querystring_msgthr_fields = new ArrayList<String>();
        querystring_msgthr_fields.add("message");
        querystring_msgthr_fields.add("throwable");
        Map<String, Object> querystring_msgthr = new HashMap<String, Object>();
        querystring_msgthr.put("query", message);
        querystring_msgthr.put("fields", querystring_msgthr_fields);
        Map<String, Map> bool_must_message = new HashMap<String, Map>();
        bool_must_message.put("query_string", querystring_msgthr);
        filtered_query_bool_must.add(bool_must_message);
    }

    // sorting
    Map<String, String> sort_fields = new HashMap<String, String>();
    sort_fields.put("@timestamp", "desc");
    List<Map> sort = new ArrayList<Map>();
    sort.add(sort_fields);

    // prepare
    HashMap<String, Object> search = new HashMap<String, Object>();
    search.put("query", query);
    search.put("from", (current - 1) * pagesize);
    search.put("size", pagesize);
    search.put("sort", sort);

    // prepare bookmark-url with query-parameters
    String bookmarkUrl = request.getRequestURL().toString();
    bookmarkUrl += "?range=" + (date != null ? range : hours);
    if (levels.length > 0) {
        bookmarkUrl += "&level=" + asString(levels);
    }
    if (logger.length() > 0) {
        bookmarkUrl += "&logger=" + logger;
    }
    if (thread.length() > 0) {
        bookmarkUrl += "&thread=" + thread;
    }
    if (message.length() > 0) {
        bookmarkUrl += "&message=" + message;
    }
    if (pagesize != 100) {
        bookmarkUrl += "&pagesize=" + pagesize;
    }
    if (auto) {
        bookmarkUrl += "&auto=on";
    }

    String exception = "";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title>logging von <%=System.getProperty("jboss.connect.address")%></title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    <link rel="stylesheet" href="css/stylesheet.css"/>
</head>
<body>
<h1>logging von <%=domain%></h1>
<%
    try {
        // call
        String jsonRequest = JSONObject.toJSONString(search);
        String jsonResponse = call(url + "/" + index + "/_search", "POST", jsonRequest, "UTF-8");
        // parse
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(jsonResponse);
        String error = (String) result.get("error");
        if (error != null) {
            exception += error;
        } else {
            Long took = (Long) result.get("took");
            JSONObject hits = (JSONObject) result.get("hits");
            entries = (Long) hits.get("total");
            pages = entries / pagesize;
            if (entries / pagesize * entries == entries) pages = pages + 1;
            if (pages < 1l) pages = 1l;
            if (current > pages) current = pages;
            JSONArray hitEntries = (JSONArray) hits.get("hits");
%>
<p>
<form id="navigation" class="navigation" action="<%=request.getRequestURL()%>" method="post">
    Filter |
    Stunden/Datum: <input type="text" name="range" value="<%=date != null ? range : hours%>" size="10"/>
    Level*: <input type="text" name="level" value="<%=asString(levels)%>" size="20"/>
    Thread**: <input type="text" name="thread" value="<%=StringEscapeUtils.escapeHtml(thread)%>" size="20"/>
    Logger**: <input type="text" name="logger" value="<%=StringEscapeUtils.escapeHtml(logger)%>" size="30"/>
    Message / Throwable**: <input type="text" name="message" value="<%=StringEscapeUtils.escapeHtml(message)%>" size="30"/> |
    Eintr&auml;ge: <input type="text" name="pagesize" value="<%=pagesize%>" size="4"/>
    <input type="submit" name="refresh" value="Aktualisieren"/> |
    <input type="hidden" name="page" value="<%=current%>"/>
    <input type="submit" name="prev" value="&lt;&lt;"<%=current == 1 ? " disabled=\"disabled\"" : ""%>/>
    <input type="submit" name="next" value="&gt;&gt;"<%=current == pages ? " disabled=\"disabled\"" : ""%>/>
    Seite <%=current%> von <%=pages%> (<%=entries%> Eintr&auml;ge) |
    <input type="checkbox" name="auto" <%=auto ? "checked " : ""%>/> AutoRefresh |
    <a href="<%=bookmarkUrl%>">Bookmark-URL</a>
    <p class="note">*Mehrere semikolon-getrennte Eintr&auml;ge mittels &quot;*&quot; m&ouml;glich | **Lucene-Query-Syntax</p>
</form>
</p>
<table border="1">
    <tr>
        <th>#</th>
        <th>Timestamp</th>
        <th>Level</th>
        <th>System</th>
        <th>Thread</th>
        <th>Logger</th>
        <th>Message / Throwable</th>
    </tr>
    <%
        for (int i = 0; i < hitEntries.size(); i++) {
            JSONObject source = (JSONObject) ((JSONObject) hitEntries.get(i)).get("_source");
            String msg = StringEscapeUtils.escapeHtml((String) source.get("message"));
            String thr = StringEscapeUtils.escapeHtml((String) source.get("throwable"));
            if (thr != null) {
                msg = (msg != null ? "<p>" + msg + "</p>" : "") + "<pre>" + thr + "</pre>";
            }
    %>
    <tr>
        <td><%=(((current - 1) * pagesize + 1) + i)%></td>
        <td><%=dateTimeFormat.format(jsonDateTimeFormat.parseObject((String) source.get("@timestamp")))%></td>
        <td class="<%=((String) source.get("level")).toLowerCase()%>"><%=(String) source.get("level")%></td>
        <td><%=(String) source.get("server")%></td>
        <td><%=(String) source.get("thread")%></td>
        <td><%=(String) source.get("logger")%></td>
        <td><%=msg != null ? msg : ""%></td>
    </tr>
    <%
                }
            }
        } catch (Exception e) {
            exception += e.getClass().getSimpleName();
            exception += ": ";
            exception += e.getMessage();
        }
    %>
</table>
<%
    if (exception.length() > 0) {
%>
<div class="error">
    <h3>Es ist ein Fehler aufgetreten</h3>
    <pre><%=exception%></pre>
</div>
<%
    }
%>
<script type="text/javascript">
    function auto() {
        var form = document.getElementById("navigation");
        if (form.auto.checked) {
            form.submit();
        } else {
            setTimeout(auto, 5000);
        }
    }
    setTimeout(auto, 5000);
</script>
</body>
</html>
