package ui;

import chess.*;
import client.ServerFacade;
import client.websocket.NotifHandler;
import client.websocket.WebSocketFacade;
import exceptions.RespExp;
import model.GameData;

import java.util.*;
import java.util.Scanner;

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
    private final Boolean isPlayer;

    public PlayClient(String serverUrl, ServerFacade server, NotifHandler notifier,
                      String username, String authToken, GameData gameData, String asType) throws RespExp {
        this.serverUrl = serverUrl;
        this.server = server;
        this.notifier = notifier;
        this.username = username;
        this.authToken = authToken;
        this.gameID = gameData.gameID();
        this.team = asType.equals("BLACK") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        this.isPlayer = !asType.equalsIgnoreCase("observer");
        this.webSocket = new WebSocketFacade(serverUrl, notifier);
        webSocket.connectToGame(authToken, gameID);
    }

    @Override
    public String eval(String input) {
        if (gameID < 0) {
            return "transition;; How did you get here? You don't even have a game saved.";
        }
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "resign" -> resign();
                case "highlight" -> highlightValidMoves(params);
                case "move" -> makeMove(params);
                default -> help();
            };
        } catch (RespExp ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage();
        }
    }

    private ChessPosition convertToPosition(String position) throws RespExp {
        if (position.length() < 2 || !position.matches("[a-h][1-8]")) {
            throw new RespExp(400, "Ensure that your positions are in File Rank format i.e. h5");
        }
        return new ChessPosition(position.charAt(1) - '0', position.charAt(0) - 'a' + 1);
    }

    private String highlightValidMoves(String... params) throws RespExp {
        if (params.length >= 1) {
            return boardPrint.highlightValidMoves(team, convertToPosition(params[0]));
        } else {
            throw new RespExp(400, "Expected: <POSITION>");
        }
    }

    private String makeMove(String... params) throws RespExp {
        if (params.length >= 2) {
            ChessPosition from = convertToPosition(params[0]);
            ChessPosition to = convertToPosition(params[1]);
            ChessPiece.PieceType promotion = null;
            if (params.length >= 3) {
                try {
                    promotion = ChessPiece.PieceType.valueOf(params[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RespExp(400, "Expected: <PROMOTION> ex: knight");
                }
            }
            ChessMove move = new ChessMove(from, to, promotion);
            webSocket.makeMove(authToken, gameID, move);
            return String.format("Successfully moved from %s to %s", from, to);
        } else {
            throw new RespExp(400, "Expected: <FROM> <TO> please provide coordinates (ex: 'c3 d5')");
        }
    }

    private String redraw() {
        return boardPrint.print(team);
    }

    private String leave() throws RespExp {
        webSocket.leaveGame(authToken, gameID);
        return "transition ;; You Left the Game";
    }

    private String resign() throws RespExp {
        System.out.println("Are you sure you want to resign yes/no");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        if (line.equalsIgnoreCase("yes") || line.equalsIgnoreCase("y")) {
            webSocket.resign(authToken, gameID);
            return "You forfeited the game";
        } else {
            return "The game shall go on";
        }
    }

    @Override
    public String loadGame(ChessGame game) {
        boardPrint.updateGame(game);
        return boardPrint.print(team);
    }

    @Override
    public String help() {
        return String.format("""
                    %s leave %s - the game
                    %s redraw %s - the board
                    %s resign %s - the game (you lose/forfeit and cannot continue) %s Players Only %s
                    %s highlight <POSITION> %s - highlight all legal moves for a given piece (fileRank i.e. a2)
                    %s move <FROM> <TO> <PROMOTION> %s - Make a Move, %s Players Only %s, <PROMOTION> for pawn promotion
                    %s quit %s - playing chess
                    %s help %s - with commands
                """,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN, SET_TEXT_BOLD, RESET_TEXT_BOLD_FAINT,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN, SET_TEXT_BOLD, RESET_TEXT_BOLD_FAINT,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN,
                SET_TEXT_COLOR_RED, SET_TEXT_COLOR_GREEN);
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
        return  "[IN-GAME]";
    }
}
