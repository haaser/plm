package de.ivz.plm.agent.nrpe.connector;

import de.ivz.plm.agent.nrpe.connector.invoker.CommandInvoker;
import de.ivz.plm.agent.nrpe.connector.invoker.MBeanInvoker;
import de.ivz.plm.agent.nrpe.connector.protocol.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SocketHandler für NRPE-Request/Response
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class SocketHandler implements Runnable {

    protected static final Logger log = Logger.getLogger(SocketHandler.class.getName());

    private Socket socket;
    private NrpeConnector nrpeConnector;

    /**
     * Konstruktur des Handler
     * @param socket der über den ServerSocket angenommenen Kommunikation
     * @param nrpeService
     */
    public SocketHandler(Socket socket, NrpeConnector nrpeService) {
        this.socket = socket;
    }

    public void run() {
        log.log(Level.FINE, "handling socket from " + socket.getRemoteSocketAddress().toString());
        InputStream input = null;
        OutputStream output = null;
        try {
            // Input & Output vom Socket holen
            input = socket.getInputStream();
            output = socket.getOutputStream();
            NrpeRequestPacket request;
            NrpeResponsePacket response;
            // Request vom Input einlesen und Anfragebehandlung durchführen
            try {
                request = new NrpeRequestPacket(input);
                response = handle(request);
            } catch (NrpeChecksumException e) {
                log.log(Level.SEVERE, "bad request crc: " + e.getMessage());
                response = new NrpeResponsePacket();
                response.setMessage("bad request crc: " + e.getMessage());
            }
            // CRC des Response aktulisieren
            response.updateCRC();
            // Response im Output ausgeben
            output.write(response.toByteArray());
            output.flush();
        } catch(EOFException e) {
            log.log(Level.FINE, "simple ping request");
        } catch(IOException e) {
            log.log(Level.SEVERE, "error reading and writing on socket", e);
        } finally {
            try {
                if (input != null) input.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "could not close input");
            }
            try {
                if (output != null) output.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "could not close output");
            }
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                log.log(Level.WARNING, "could not close socket");
            }
            socket = null;
            nrpeConnector = null;
        }
    }

    /**
     * Behandlungsroutine für des angenommenen NRPE-Request
     * @param request der NRPE-Request
     * @return der entsprechend dem durchgeführten Check generierte NRPE-Response
     */
    private NrpeResponsePacket handle(NrpeRequestPacket request) {
        // parsen des Commandos und der Parameter im Request
        String[] parts = request.getMessage().split("!");
        String command = parts[0];
        String[] arguments = new String[parts.length - 1];
        System.arraycopy(parts, 1, arguments, 0, arguments.length);
        log.log(Level.FINE, "handling request: type='" + request.getType() + "', command='" + command + "', arguments=" + Arrays.toString(arguments));
        // prepare response
        NrpeResponsePacket response = new NrpeResponsePacket();
        if (request.getType() == NrpePacket.TYPE_QUERY) {
            try {
                // ermittle den BSH-Code für das Kommando
                String commandCode = nrpeConnector.getCommand(command);
                MBeanInvoker mbeanInvoker = new MBeanInvoker();
                CommandInvoker commandInvoker = new CommandInvoker(commandCode, mbeanInvoker);
                // führe den BSH-Code
                NrpeReturnValue returnValue = commandInvoker.invoke(arguments);
                if (returnValue != null) {
                    response.setCode(returnValue.getCode());
                    response.setMessage(returnValue.getMessage());
                } else {
                    response.setMessage("unsupported command '" + command + "'");
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "command handling failed", e);
                response.setMessage("command handling failed: " + e.getMessage());
            }
        } else {
            log.log(Level.SEVERE, "invalid packet type='" + request.getType() + "'");
            response.setMessage("invalid packet type='" + request.getType() + "'");
        }
        return response;
    }
}
