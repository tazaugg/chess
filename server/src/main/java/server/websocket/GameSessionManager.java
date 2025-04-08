package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameSessionManager {
    private final Map<Integer, List<Connection>> activeSessions = new ConcurrentHashMap<>();
    private final Gson serializer = new Gson();

    public void registerConnection(int gameId, Connection client) {
        activeSessions.computeIfAbsent(gameId, id -> new CopyOnWriteArrayList<>()).add(client);
    }

    public void unregisterConnection(int gameId, Connection client) {
        List<Connection> group = activeSessions.get(gameId);
        if (group != null) {
            group.remove(client);
            if (group.isEmpty()) {
                activeSessions.remove(gameId);
            }
        }
    }

    public List<Connection> listConnections(int gameId) {
        return activeSessions.getOrDefault(gameId, Collections.emptyList());
    }

    public void sendToAllExcept(int gameId, String excludedUser, ServerMessage msg) throws IOException {
        List<Connection> clients = activeSessions.getOrDefault(gameId, Collections.emptyList());
        List<Connection> disconnected = new ArrayList<>();

        for (Connection client : clients) {
            Session s = client.getSession();
            String user = client.getUserName();

            if (s.isOpen()) {
                if (!user.equals(excludedUser)) {
                    client.sendMessage(serializer.toJson(msg));
                }
            } else {
                disconnected.add(client);
            }
        }

        clients.removeAll(disconnected);
    }
}
