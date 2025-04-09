package ui;

import chess.ChessGame;
import client.ServerFacade;
import client.websocket.NotifHandler;
import client.websocket.WebSocketFacade;
import exceptions.RespExp;
import model.GameData;

import static ui.EscapeSequences.*;

public class PlayClient implements Client {
    private final String serverUrl;
    private final ServerFacade server;
    private final NotifHandler notifier;
    private final WebSocketFacade webSocket;
    private final String username;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor team;
    private final BoardPrint boardPrint = new BoardPrint();
    private ChessGame game;

    public PlayClient(String serverUrl, ServerFacade server, NotifHandler notifier,
                      String username, String authToken, GameData gameData) throws RespExp {
        this.serverUrl = serverUrl;
        this.server = server;
        this.notifier = notifier;
        this.webSocket = new WebSocketFacade(serverUrl, notifier);
        this.username = username;
        this.authToken = authToken;
        this.gameID = gameData.gameID();
        this.game = gameData.game();
        this.team = gameData.blackUsername().equals(username) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        webSocket.connectToGame(authToken, gameID);
    }

    @Override
    public String eval(String input) {
        if (game == null || gameID < 0) {
            return "transition;; How did you get here? You don't even have a game saved.";
        }
        return "transition;; How did you get here? This isn't even implemented yet.";
    }

    @Override
    public String loadGame(ChessGame game) {
        boardPrint.updateGame(game);
        return boardPrint.print(team);
    }

    @Override
    public String help() {
        return String.format("""
                     %s create <NAME> %s - a game
                     %s list %s - games
                     %s join <ID> [WHITE|BLACK] %s - a game
                     %s observe <ID> %s - a game
                     %s logout %s - when your done
                     %s quit %s - playing chess
                     %s help %s - with commands
                 """,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN
        );
    }

    @Override
    public Client transition(String token) {
        return this;
    }

    @Override
    public Client transition() {
        return new PostLogClient(serverUrl, server, notifier, username, authToken);
    }

    @Override
    public String printState() {
        return boardPrint.print(team) + "\n" + RESET + "[IN-GAME]";
    }
}
