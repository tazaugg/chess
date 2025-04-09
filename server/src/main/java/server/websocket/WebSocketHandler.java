package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import exceptions.RespExp;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserAuthService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final GameService gameService;
    private final UserAuthService userService;
    private final ConnectionManager connectionManager = new ConnectionManager();

    public WebSocketHandler(GameService gameService, UserAuthService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String rawMessage) throws IOException {
        Gson gson = new Gson();
        UserGameCommand command = gson.fromJson(rawMessage, UserGameCommand.class);

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
                case CONNECT -> handleConnect(session, rawMessage);
                case LEAVE -> handleLeave(session, rawMessage);
                case MAKE_MOVE -> handleMakeMove(session, rawMessage);
                case RESIGN -> handleResign(session, rawMessage);
                default -> sendError(session, "Error: Unknown command type");
            }

        } catch (RespExp | InvalidMoveException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleConnect(Session session, String message) throws RespExp, IOException {
        ConnectCommand connect = new Gson().fromJson(message, ConnectCommand.class);
        String user = userService.getUsername(connect.getAuthToken());
        int gameId = connect.getGameID();
        GameData game = gameService.getGame(gameId);

        connectionManager.add(gameId, new Connection(session, user));

        String role = "an Observer";
        if (user.equals(game.whiteUsername())) {
            role = "White Player";
        } else if (user.equals(game.blackUsername())) {
            role = "Black Player";
        }

        String note = String.format("%s joined the game as %s", user, role);
        NotificationMessage notify = new NotificationMessage(note);
        connectionManager.broadcast(gameId, user, notify);
    }

    private void handleLeave(Session session, String message) {
        LeaveCommand leave = new Gson().fromJson(message, LeaveCommand.class);
        // TODO: Remove the connection and notify others if needed
    }

    private void handleMakeMove(Session session, String message) throws InvalidMoveException, RespExp {
        MakeMoveCommand move = new Gson().fromJson(message, MakeMoveCommand.class);
        String authToken = move.getAuthToken();
        GameData gameData = gameService.getGame(move.getGameID());
        String username = userService.getUsername(authToken);
        ChessGame board = gameData.game();

        ChessGame.TeamColor turn = board.getTeamTurn();

        boolean isCorrectWhite = turn == ChessGame.TeamColor.WHITE && username.equals(gameData.whiteUsername());
        boolean isCorrectBlack = turn == ChessGame.TeamColor.BLACK && username.equals(gameData.blackUsername());

        if (isCorrectWhite || isCorrectBlack) {
            board.makeMove(move.retrieveMove());

            GameData updated = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    board
            );

            gameService.updateGame(updated);
            // TODO: Notify all players of the move
        } else {
            throw new InvalidMoveException("Error: it is not your turn or you are not a player");
        }
    }

    private void handleResign(Session session, String message) {
        ResignCommand resign = new Gson().fromJson(message, ResignCommand.class);
        // TODO: Mark the game as over and broadcast a resignation notice
    }

    private void sendError(Session session, String errorText) throws IOException {
        ErrorMessage error = new ErrorMessage(errorText);
        session.getRemote().sendString(new Gson().toJson(error, ErrorMessage.class));
    }
}
