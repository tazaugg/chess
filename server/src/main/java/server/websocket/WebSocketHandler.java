package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import exceptions.RespExp;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import service.GameService;
import service.UserAuthService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;

import java.io.IOException;

public class WebSocketHandler {
    private final GameService gameService;
    private final UserAuthService userService;

    public WebSocketHandler(GameService gameService, UserAuthService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

        try {
            if (!userService.verifyToken(command.getAuthToken())) {
                sendError(session, "Error: Invalid Token");
                return;
            }

            if (gameService.getGame(command.getGameID()) == null) {
                sendError(session, "Error: Game does not exist");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, message);
                case LEAVE -> handleLeave(session, message);
                case MAKE_MOVE -> handleMakeMove(session, message);
                case RESIGN -> handleResign(session, message);
            }

        } catch (RespExp | InvalidMoveException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleConnect(Session session, String message) {
        ConnectCommand command = new Gson().fromJson(message, ConnectCommand.class);
        // TODO: Implement game join + WebSocket connect logic
    }

    private void handleLeave(Session session, String message) {
        LeaveCommand command = new Gson().fromJson(message, LeaveCommand.class);
        // TODO: Implement game leave logic and update clients
    }

    private void handleMakeMove(Session session, String message) throws InvalidMoveException, RespExp {
        MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
        String authToken = command.getAuthToken();
        GameData gameData = gameService.getGame(command.getGameID());

        String username = userService.getUsername(authToken);
        ChessGame.TeamColor currentTurn = gameData.game().getTeamTurn();

        boolean isWhite = currentTurn == ChessGame.TeamColor.WHITE && username.equals(gameData.whiteUsername());
        boolean isBlack = currentTurn == ChessGame.TeamColor.BLACK && username.equals(gameData.blackUsername());

        if (isWhite || isBlack) {
            ChessGame updatedGame = gameData.game();
            updatedGame.makeMove(command.retrieveMove());

            gameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    updatedGame
            );

            // TODO: Broadcast updated board and notification
        } else {
            throw new InvalidMoveException("Error: it is not your turn or you are not a player");
        }
    }

    private void handleResign(Session session, String message) {
        ResignCommand command = new Gson().fromJson(message, ResignCommand.class);
        // TODO: Mark game as resigned and notify all clients
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        ErrorMessage error = new ErrorMessage(errorMessage);
        session.getRemote().sendString(new Gson().toJson(error, ErrorMessage.class));
    }
}
