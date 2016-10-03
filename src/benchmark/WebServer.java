package benchmark;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;


public class WebServer extends WebSocketServer {

    public static final int PORT = 8887;

    public WebServer() throws UnknownHostException {
        super(new InetSocketAddress(PORT));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[INFO] WebSocket connected to WebServer");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        e.printStackTrace();
    }

    public void sendToAll(String message) {
        Collection<WebSocket> conns = connections();
        synchronized (conns) {
            for (WebSocket conn : conns) {
                conn.send(message);
                System.out.printf("[INFO] Send message %s to WebSocket\n", message);
            }
        }
    }
}
