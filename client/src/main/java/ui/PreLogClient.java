package ui;

import exceptions.RespExp;
import model.AuthData;
import client.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class PreLogClient implements Client {
    private final String serverUrl;
    private final ServerFacade server;

    public PreLogClient(String serverUrl) {
        this(serverUrl, new ServerFacade(serverUrl));
    }

    public PreLogClient(String serverUrl, ServerFacade server) {
        this.serverUrl = serverUrl;
        this.server = server;
    }

    @Override
    public String eval(String input) {
        try {
            var tokens = input.trim().toLowerCase().split("\\s+");
            if (tokens.length == 0) return displayHelp();

            var command = tokens[0];
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (command) {
                case "register" -> attemptRegister(params);
                case "login" -> attemptLogin(params);
                case "quit" -> "quit";
                default -> displayHelp();
            };
        } catch (RespExp ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage();
        }
    }

    private String attemptLogin(String... credentials) throws RespExp {
        if (credentials.length < 2) {
            throw new RespExp(400, "Usage: <USERNAME> <PASSWORD>");
        }
        var authData = server.login(credentials[0], credentials[1]);
        return formatTransitionMessage(authData);
    }

    private String attemptRegister(String... details) throws RespExp {
        if (details.length < 3) {
            throw new RespExp(400, "Usage: <USERNAME> <PASSWORD> <EMAIL>");
        }
        var authData = server.register(details[0], details[1], details[2]);
        return formatTransitionMessage(authData);
    }

    private String formatTransitionMessage(AuthData authData) {
        return String.format("transition ;; %s, %s ;; Successfully logged in as %s",
                authData.username(), authData.authToken(), authData.username());
    }

    @Override
    public String help() {
        return displayHelp();
    }

    private String displayHelp() {
        return String.format("""
                    %s register <USERNAME> <PASSWORD> <EMAIL> %s- Create a new account
                    %s login <USERNAME> <PASSWORD> %s- Sign in to play chess
                    %s quit %s- Exit the application
                    %s help %s- Show available commands
                """,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA
        );
    }

    @Override
    public Client transition(String token) {
        var tokens = token.split(",");
        return new PostLogClient(serverUrl, server, tokens[0].trim(), tokens[1].trim());
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
