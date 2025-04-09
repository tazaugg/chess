package client.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import exceptions.RespExp;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private final NotifHandler notifier;
    private static final Gson gson = new Gson();

    public WebSocketFacade(String baseUrl, NotifHandler notifier) throws RespExp {
        this.notifier = notifier;
        try {
            String socketUrl = baseUrl.replace("http", "ws") + "/ws";
            URI endpoint = new URI(socketUrl);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, endpoint);

            this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleIncomingMessage);
        } catch (IOException | DeploymentException | URISyntaxException e) {
            throw new RespExp(500, "WebSocket connection failed: " + e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    private void handleIncomingMessage(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        switch (base.getServerMessageType()) {
            case NOTIFICATION -> notifier.handleNotif(gson.fromJson(message, NotificationMessage.class));
            case ERROR -> notifier.handleWarning(gson.fromJson(message, ErrorMessage.class));
            case LOAD_GAME -> notifier.loadGame(gson.fromJson(message, LoadGameMessage.class));
        }
    }

    public void connectToGame(String authToken, int gameId) throws RespExp {
        sendCommand(new ConnectCommand(authToken, gameId));
    }

    public void leaveGame(String authToken, int gameId) throws RespExp {
        sendCommand(new LeaveCommand(authToken, gameId));
    }

    public void makeMove(String authToken, int gameId, ChessMove move) throws RespExp {
        sendCommand(new MakeMoveCommand(authToken, gameId, move));
    }

    public void resign(String authToken, int gameId) throws RespExp {
        sendCommand(new ResignCommand(authToken, gameId));
    }

    private void sendCommand(Object command) throws RespExp {
        try {
            String json = gson.toJson(command);
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            throw new RespExp(500, "Failed to send command: " + e.getMessage());
        }
    }
}
