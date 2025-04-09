package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionManager {
    private final Map<Integer, List<Connection>> sessionsByGame = new ConcurrentHashMap<>();
    private final Gson serializer = new Gson();

    public ConnectionManager() {}

    public void add(int gameID, Connection connection) {
        sessionsByGame.computeIfAbsent(gameID, k -> new CopyOnWriteArrayList<>()).add(connection);
    }

    public void remove(int gameID, Connection connection) {
        List<Connection> group = sessionsByGame.get(gameID);
        if (group != null) {
            group.remove(connection);
            if (group.isEmpty()) {
                sessionsByGame.remove(gameID);
            }
        }
    }

    public void remove(int gameID, Session session, String username) {
        List<Connection> group = sessionsByGame.get(gameID);
        if (group != null) {
            for (Connection connection : group) {
                if (connection.getSession().equals(session) && connection.getUserName().equals(username)) {
                    group.remove(connection);
                    break;
                }
            }
            if (group.isEmpty()) {
                sessionsByGame.remove(gameID);
            }
        }
    }



    public void broadcast(int gameID, String excludedUsername, ServerMessage message) throws IOException {
        List<Connection> participants = sessionsByGame.getOrDefault(gameID, Collections.emptyList());
        List<Connection> toRemove = new ArrayList<>();

        for (Connection client : participants) {
            Session session = client.getSession();
            String username = client.getUserName();

            if (session.isOpen()) {
                if (!username.equals(excludedUsername)) {
                    client.sendMessage(serializer.toJson(message));
                }
            } else {
                toRemove.add(client);
            }
        }

        participants.removeAll(toRemove);
    }
}
