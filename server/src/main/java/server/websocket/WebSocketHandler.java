package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
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
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final GameService gameService;
    private final UserAuthService userService;
    private final ConnectionManager connectionManager = new ConnectionManager();
    private static final Gson GSON = new Gson();

    public WebSocketHandler(GameService gameService, UserAuthService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String rawMessage) throws IOException {
        UserGameCommand command = GSON.fromJson(rawMessage, UserGameCommand.class);

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
        ConnectCommand command = GSON.fromJson(message, ConnectCommand.class);
        String username = userService.getUsername(command.getAuthToken());
        int gameId = command.getGameID();
        GameData gameData = gameService.getGame(gameId);

        connectionManager.add(gameId, new Connection(session, username));
        session.getRemote().sendString(GSON.toJson(new LoadGameMessage(gameData.game())));

        String role = "an Observer";
        if (username.equals(gameData.whiteUsername())) {
            role = "White Player";
        } else if (username.equals(gameData.blackUsername())) {
            role = "Black Player";
        }

        NotificationMessage note = new NotificationMessage(
                String.format("%s joined the game as %s", username, role)
        );
        connectionManager.broadcast(gameId, username, note);
    }

    private void handleLeave(Session session, String message) throws RespExp, IOException {
        LeaveCommand command = GSON.fromJson(message, LeaveCommand.class);
        int gameId = command.getGameID();
        String username = userService.getUsername(command.getAuthToken());
        GameData gameData = gameService.getGame(gameId);

        GameData updated = new GameData(
                gameId,
                username.equals(gameData.whiteUsername()) ? null : gameData.whiteUsername(),
                username.equals(gameData.blackUsername()) ? null : gameData.blackUsername(),
                gameData.gameName(),
                gameData.game()
        );
        gameService.updateGame(updated);
        connectionManager.remove(gameId, session, username);

        NotificationMessage note = new NotificationMessage(String.format("%s has left the game", username));
        connectionManager.broadcast(gameId, username, note);
    }

    private void handleMakeMove(Session session, String message) throws InvalidMoveException, RespExp, IOException {
        MakeMoveCommand command = GSON.fromJson(message, MakeMoveCommand.class);
        String username = userService.getUsername(command.getAuthToken());
        GameData gameData = gameService.getGame(command.getGameID());
        ChessGame board = gameData.game();

        if (board.isGameOver()) {
            sendError(session, "Error: cannot make move, the game is over");
            return;
        }

        ChessGame.TeamColor turn = board.getTeamTurn();
        boolean correctTurn = (turn == ChessGame.TeamColor.WHITE && username.equals(gameData.whiteUsername())) ||
                (turn == ChessGame.TeamColor.BLACK && username.equals(gameData.blackUsername()));

        if (!correctTurn) {
            throw new InvalidMoveException("Error: it is not your turn or you are not a player");
        }

        ChessMove move = command.retrieveMove();
        board.makeMove(move);

        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                board
        );
        gameService.updateGame(updated);

        connectionManager.broadcast(gameData.gameID(), null, new LoadGameMessage(board));

        connectionManager.broadcast(gameData.gameID(), username, new NotificationMessage(
                String.format("%s, moved from %s to %s. It is now %s's turn.",
                        username,
                        move.getStartPosition().prettyOutput(),
                        move.getEndPosition().prettyOutput(),
                        board.getTeamTurn())
        ));

        if (board.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            connectionManager.broadcast(gameData.gameID(), null, new NotificationMessage(
                    String.format("%s (White) is in Checkmate. %s (Black) wins!",
                            gameData.whiteUsername(), gameData.blackUsername())));
        } else if (board.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            connectionManager.broadcast(gameData.gameID(), null, new NotificationMessage(
                    String.format("%s (Black) is in Checkmate. %s (White) wins!",
                            gameData.blackUsername(), gameData.whiteUsername())));
        } else if (board.isInCheck(ChessGame.TeamColor.WHITE)) {
            connectionManager.broadcast(gameData.gameID(), null, new NotificationMessage(
                    String.format("%s (White) is in check.", gameData.whiteUsername())));
        } else if (board.isInCheck(ChessGame.TeamColor.BLACK)) {
            connectionManager.broadcast(gameData.gameID(), null, new NotificationMessage(
                    String.format("%s (Black) is in check.", gameData.blackUsername())));
        } else if (board.isInStalemate(board.getTeamTurn())) {
            connectionManager.broadcast(gameData.gameID(), null, new NotificationMessage(
                    "Stalemate! No legal moves and no check. Game ends in a draw."));
        }
    }

    private void handleResign(Session session, String message) throws RespExp, IOException {
        ResignCommand command = GSON.fromJson(message, ResignCommand.class);
        String username = userService.getUsername(command.getAuthToken());
        GameData gameData = gameService.getGame(command.getGameID());
        ChessGame board = gameData.game();

        if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
            sendError(session, "Error: Only players can resign");
            return;
        }

        if (board.isGameOver()) {
            sendError(session, "Error: Game is already over");
            return;
        }

        board.setGameOver(true);
        gameService.updateGame(new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                board
        ));

        connectionManager.broadcast(command.getGameID(), null, new NotificationMessage(
                String.format("%s has resigned. The game is over.", username)));
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        ErrorMessage error = new ErrorMessage(errorMessage);
        session.getRemote().sendString(GSON.toJson(error, ErrorMessage.class));
    }
}
