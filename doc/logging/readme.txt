Konfiguration der Erweiterungen für die Logging-Frameworks

Handler (Java Logging)
-----------------------
<custom-handler name="UdpHandler" class="de.ivz.plm.logging.UdpHandler" module="de.ivz.plm">
    <properties>
        <!--property name="localAddress" value="127.0.0.1"/-->
        <!--property name="localPort" value="-1"/-->
        <property name="remoteAddress" value="my.udp.host"/>
        <property name="remotePort" value="4711"/>
        <property name="userFields" value="key1:static_value,key2:${variable.name}"/>
        <!--property name="encodeCharset" value="UTF-8"/-->
    </properties>
</custom-handler>

Appender (Log4J)
------------------------
<appender name="UdpAppender" class="de.ivz.plm.logging.UdpAppender">
    <!--param name="LocalAddress" value="127.0.0.1"/-->
    <!--param name="LocalPort" value="-1"/-->
    <param name="RemoteAddress" value="my.udp.host"/>
    <param name="RemotePort" value="4711"/>
    <param name="UserFields" value="key1:static_value,key2:${variable.name}"/>
    <!--property name="encodeCharset" value="UTF-8"/-->
</appender>


