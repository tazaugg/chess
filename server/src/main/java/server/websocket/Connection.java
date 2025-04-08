package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    private final Session session;
    private final String userName;

    public Connection(Session session, String userName) {
        this.session = session;
        this.userName = userName;
    }

    public Session getSession() {
        return session;
    }

    public String getUserName() {
        return userName;
    }

    public void sendMessage(String message) throws IOException {
        session.getRemote().sendString(message);
    }
}
