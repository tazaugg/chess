package server.websocket;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import exceptions.RespExp;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import service.GameService;
import service.UserAuthService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;

import java.io.IOException;

public class WebSocketHandler {
    private final GameService games;
    private final UserAuthService users;

    public WebSocketHandler(GameService games, UserAuthService users) {
        this.games = games;
        this.users = users;
    }

    @OnWebSocketMessage
    public void handleIncomingMessage(Session userSession, String inputJson) throws IOException {
        Gson gson = new Gson();
        UserGameCommand baseCommand = gson.fromJson(inputJson, UserGameCommand.class);

        try {
            if (!users.verifyToken(baseCommand.getAuthToken())) {
                deliverError(userSession, "Error: Invalid token.");
                return;
            }

            if (games.getGame(baseCommand.getGameID()) == null) {
                deliverError(userSession, "Error: Game not found.");
                return;
            }

            switch (baseCommand.getCommandType()) {
                case CONNECT -> processConnect(userSession, inputJson);
                case LEAVE -> processLeave(userSession, inputJson);
                case MAKE_MOVE -> processMove(userSession, inputJson);
                case RESIGN -> processResign(userSession, inputJson);
            }

        } catch (RespExp | InvalidMoveException ex) {
            deliverError(userSession, "Error: " + ex.getMessage());
        }
    }

    private void processConnect(Session session, String json) {
        ConnectCommand connect = new Gson().fromJson(json, ConnectCommand.class);
        // TODO: handle connect logic
    }

    private void processLeave(Session session, String json) {
        LeaveCommand leave = new Gson().fromJson(json, LeaveCommand.class);
        // TODO: handle leave logic
    }

    private void processMove(Session session, String json) throws InvalidMoveException {
        MakeMoveCommand moveCommand = new Gson().fromJson(json, MakeMoveCommand.class);
        // TODO: handle make move logic
    }

    private void processResign(Session session, String json) {
        ResignCommand resign = new Gson().fromJson(json, ResignCommand.class);
        // TODO: handle resign logic
    }

    private void deliverError(Session session, String errorText) throws IOException {
        ErrorMessage errorPayload = new ErrorMessage(errorText);
        session.getRemote().sendString(new Gson().toJson(errorPayload, ErrorMessage.class));
    }
}
