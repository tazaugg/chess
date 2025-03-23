package ui;

import chess.ChessGame;
import model.GameData;
import client.ServerFacade;

import static ui.EscapeSequences.RESET;

public class PlayClient implements Client {
    private final String serverUrl;
    private final ServerFacade server;
    private final String username;
    private final String authToken;
    private ChessGame game;
    private final ChessGame.TeamColor team;
    private final int gameID;
    private final BoardPrint boardPrint;

    PlayClient(String serverUrl, ServerFacade server, String username, String authToken, GameData gameData) {
        this.serverUrl = serverUrl;
        this.server = server;
        this.username = username;
        this.authToken = authToken;
        this.game = gameData.game();
        this.gameID = gameData.gameID();
        this.team = gameData.blackUsername().equals(username) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        this.boardPrint = new BoardPrint(gameData.game());
    }

    @Override
    public String eval(String input) {
        if (game == null || gameID < 0) {
            return "transition;; How did you get here? You don't even have a game saved.";
        }
        return "transition;; How did you get here? This isn't even implemented yet.";
    }

    @Override
    public String help() {
        return "";
    }

    @Override
    public Client transition(String token) {
        return this;
    }

    @Override
    public Client transition() {
        return new PostLogClient(serverUrl, server, username, authToken);
    }

    @Override
    public String printState() {
        return boardPrint.print(team) + "\n" + RESET + "[IN-GAME]";
    }
}
