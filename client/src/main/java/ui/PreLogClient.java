package ui;

import chess.ChessGame;
import client.websocket.NotifHandler;
import exceptions.RespExp;
import model.AuthData;
import client.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class PreLogClient implements Client {
    private final String serverUrl;
    private final NotifHandler notifier;
    private final ServerFacade server;

    public PreLogClient(String serverUrl, NotifHandler notifier) {
        this.serverUrl = serverUrl;
        this.notifier = notifier;
        this.server = new ServerFacade(serverUrl);
    }

    @Override
    public String eval(String input) {
        try {
            var tokens = input.trim().toLowerCase().split("\\s+");
            if (tokens.length == 0) {
                return help();
            }
            var command = tokens[0];
            var args = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "register" -> register(args);
                case "login" -> login(args);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (RespExp ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage();
        }
    }

    @Override
    public String loadGame(ChessGame game) {
        return "";
    }

    private String login(String... credentials) throws RespExp {
        if (credentials.length < 2) {
            throw new RespExp(400, "Expected: <USERNAME> <PASSWORD>");
        }
        AuthData authData = server.login(credentials[0], credentials[1]);
        return String.format("transition ;; %s, %s ;; Logged in as %s",
                authData.username(), authData.authToken(), authData.username());
    }

    private String register(String... details) throws RespExp {
        if (details.length < 3) {
            throw new RespExp(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
        }
        AuthData authData = server.register(details[0], details[1], details[2]);
        return String.format("transition ;; %s, %s ;; Logged in as %s",
                authData.username(), authData.authToken(), authData.username());
    }

    @Override
    public String help() {
        return String.format("""
                    %s register <USERNAME> <PASSWORD> <EMAIL> %s- Create a new account
                    %s login <USERNAME> <PASSWORD> %s- Sign in to play chess
                    %s quit %s- Exit the application
                    %s help %s- Show available commands
                """,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA);
    }

    @Override
    public Client transition(String token) {
        String[] parts = token.split(",");
        return new PostLogClient(serverUrl, server, notifier, parts[0].trim(), parts[1].trim());

    }

    @Override
    public Client transition() {
        return this;
    }

    @Override
    public String printState() {
        return "[LOGGED_OUT]";
    }
}
