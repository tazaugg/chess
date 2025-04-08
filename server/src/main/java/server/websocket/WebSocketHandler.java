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
    public void onMessage(Session session, String rawMessage) throws IOException {
        Gson parser = new Gson();
        UserGameCommand baseCommand = parser.fromJson(rawMessage, UserGameCommand.class);

        try {
            if (!userService.verifyToken(baseCommand.getAuthToken())) {
                sendError(session, "Error: Invalid Token");
                return;
            }

            if (gameService.getGame(baseCommand.getGameID()) == null) {
                sendError(session, "Error: Game does not exist");
                return;
            }

            switch (baseCommand.getCommandType()) {
                case CONNECT -> handleConnect(session, rawMessage);
                case LEAVE -> handleLeave(session, rawMessage);
                case MAKE_MOVE -> handleMakeMove(session, rawMessage);
                case RESIGN -> handleResign(session, rawMessage);
            }

        } catch (RespExp | InvalidMoveException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleConnect(Session session, String message) {
        ConnectCommand connectData = new Gson().fromJson(message, ConnectCommand.class);
        // TODO: Connect user to game and initialize board display
    }

    private void handleLeave(Session session, String message) {
        LeaveCommand leaveData = new Gson().fromJson(message, LeaveCommand.class);
        // TODO: Remove user from game and notify others
    }

    private void handleMakeMove(Session session, String message) throws InvalidMoveException, RespExp {
        MakeMoveCommand moveData = new Gson().fromJson(message, MakeMoveCommand.class);
        String token = moveData.getAuthToken();
        int gameId = moveData.getGameID();
        GameData currentGameData = gameService.getGame(gameId);
        String currentUser = userService.getUsername(token);
        ChessGame board = currentGameData.game();

        ChessGame.TeamColor turn = board.getTeamTurn();

        boolean correctWhite = turn == ChessGame.TeamColor.WHITE && currentUser.equals(currentGameData.whiteUsername());
        boolean correctBlack = turn == ChessGame.TeamColor.BLACK && currentUser.equals(currentGameData.blackUsername());

        if (correctWhite || correctBlack) {
            board.makeMove(moveData.retrieveMove());

            GameData updatedGame = new GameData(
                    currentGameData.gameID(),
                    currentGameData.whiteUsername(),
                    currentGameData.blackUsername(),
                    currentGameData.gameName(),
                    board
            );

            gameService.updateGame(updatedGame);

            // TODO: Broadcast update to all clients
        } else {
            throw new InvalidMoveException("Error: it is not your turn or you are not a player");
        }
    }

    private void handleResign(Session session, String message) {
        ResignCommand resignData = new Gson().fromJson(message, ResignCommand.class);
        // TODO: Handle resignation and notify clients
    }

    private void sendError(Session session, String errorMsg) throws IOException {
        ErrorMessage error = new ErrorMessage(errorMsg);
        session.getRemote().sendString(new Gson().toJson(error, ErrorMessage.class));
    }
}
