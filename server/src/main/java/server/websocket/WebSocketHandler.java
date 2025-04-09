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
    private static final Gson gson = new Gson();

    public WebSocketHandler(GameService gameService, UserAuthService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String rawMessage) throws IOException {
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
        ConnectCommand connect = gson.fromJson(message, ConnectCommand.class);
        String user = userService.getUsername(connect.getAuthToken());
        int gameId = connect.getGameID();
        GameData game = gameService.getGame(gameId);

        connectionManager.add(gameId, new Connection(session, user));

        LoadGameMessage loadGame = new LoadGameMessage(game.game());
        session.getRemote().sendString(gson.toJson(loadGame));

        String role = "an Observer";
        if (user.equals(game.whiteUsername())) {
            role = "White Player";
        } else if (user.equals(game.blackUsername())) {
            role = "Black Player";
        }

        NotificationMessage joined = new NotificationMessage(
                String.format("%s joined the game as %s", user, role)
        );
        connectionManager.broadcast(gameId, user, joined);
    }

    private void handleLeave(Session session, String message) {
        LeaveCommand leave = gson.fromJson(message, LeaveCommand.class);
        // TODO: Remove connection and notify others
    }

    private void handleMakeMove(Session session, String message) throws InvalidMoveException, RespExp, IOException {
        MakeMoveCommand move = gson.fromJson(message, MakeMoveCommand.class);
        String authToken = move.getAuthToken();
        int gameId = move.getGameID();
        GameData gameData = gameService.getGame(gameId);
        String user = userService.getUsername(authToken);
        ChessGame board = gameData.game();
        ChessGame.TeamColor currentTurn = board.getTeamTurn();

        boolean isWhiteTurn = currentTurn == ChessGame.TeamColor.WHITE && user.equals(gameData.whiteUsername());
        boolean isBlackTurn = currentTurn == ChessGame.TeamColor.BLACK && user.equals(gameData.blackUsername());

        if (isWhiteTurn || isBlackTurn) {
            ChessMove actualMove = move.retrieveMove();
            board.makeMove(actualMove);

            GameData updated = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    board
            );
            gameService.updateGame(updated);

            connectionManager.broadcast(gameId, null, new LoadGameMessage(board));

            NotificationMessage moveNote = new NotificationMessage(
                    String.format("%s moved from %s to %s. It is now %s's turn.",
                            user,
                            actualMove.getStartPosition().prettyOutput(),
                            actualMove.getEndPosition().prettyOutput(),
                            board.getTeamTurn()
                    )
            );
            connectionManager.broadcast(gameId, user, moveNote);

            if (board.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                connectionManager.broadcast(gameId, null, new NotificationMessage(
                        String.format("%s (White) is in checkmate. %s (Black) wins!",
                                gameData.whiteUsername(), gameData.blackUsername())
                ));
            } else if (board.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                connectionManager.broadcast(gameId, null, new NotificationMessage(
                        String.format("%s (Black) is in checkmate. %s (White) wins!",
                                gameData.blackUsername(), gameData.whiteUsername())
                ));
            }
            else if (board.isInCheck(ChessGame.TeamColor.WHITE)) {
                connectionManager.broadcast(gameId, null, new NotificationMessage(
                        String.format("%s (White) is in check.", gameData.whiteUsername())
                ));
            } else if (board.isInCheck(ChessGame.TeamColor.BLACK)) {
                connectionManager.broadcast(gameId, null, new NotificationMessage(
                        String.format("%s (Black) is in check.", gameData.blackUsername())
                ));
            }
            else if (board.isInStalemate(board.getTeamTurn())) {
                connectionManager.broadcast(gameId, null, new NotificationMessage(
                        "Stalemate! No valid moves and no check. The game ends in a draw."
                ));
            }

        } else {
            throw new InvalidMoveException("Error: it is not your turn or you are not a player");
        }
    }

    private void handleResign(Session session, String message) {
        ResignCommand resign = gson.fromJson(message, ResignCommand.class);
        // TODO: Mark the game as resigned and notify all players
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        ErrorMessage error = new ErrorMessage(errorMessage);
        session.getRemote().sendString(gson.toJson(error, ErrorMessage.class));
    }
}
