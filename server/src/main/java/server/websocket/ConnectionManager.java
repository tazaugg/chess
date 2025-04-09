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

    public List<Connection> getAll(int gameID) {
        return sessionsByGame.getOrDefault(gameID, Collections.emptyList());
    }

    public void broadcast(int gameID, String excludedUsername, ServerMessage message) throws IOException {
        List<Connection> participants = sessionsByGame.getOrDefault(gameID, Collections.emptyList());
        List<Connection> toRemove = new ArrayList<>();

        for (Connection participant : participants) {
            Session s = participant.getSession();
            String user = participant.getUserName();

            if (s.isOpen()) {
                if (!user.equals(excludedUsername)) {
                    participant.sendMessage(serializer.toJson(message));
                }
            } else {
                toRemove.add(participant);
            }
        }

        participants.removeAll(toRemove);
    }
}
