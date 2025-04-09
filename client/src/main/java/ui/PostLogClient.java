package ui;

import chess.ChessGame;
import client.ServerFacade;
import client.websocket.NotifHandler;
import exceptions.RespExp;
import model.GameData;

import java.util.*;

import static ui.EscapeSequences.*;

public class PostLogClient implements Client {
    private final String serverUrl;
    private final ServerFacade server;
    private final NotifHandler notifier;
    private final String authToken;
    private final String username;
    private final List<GameData> games;

    public PostLogClient(String serverUrl, ServerFacade server, NotifHandler notifier, String username, String authToken) {
        this.serverUrl = serverUrl;
        this.server = server;
        this.notifier = notifier;
        this.authToken = authToken;
        this.username = username;
        this.games = new ArrayList<>();
    }

    @Override
    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = tokens.length > 0 ? tokens[0] : "help";
            var args = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create" -> create(args);
                case "list" -> list();
                case "join" -> join(args);
                case "observe" -> observe(args);
                case "logout" -> logout();
                case "quit" -> quit();
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

    public String quit() {
        try {
            server.logout(authToken);
        } catch (RespExp ignored) {}
        return "quit";
    }

    public String create(String... args) throws RespExp {
        if (args.length < 1) throw new RespExp(400, "Expected: <NAME>");
        server.createGame(args[0], authToken);
        return "Successfully created game";
    }

    public String list() throws RespExp {
        games.clear();
        games.addAll(Arrays.asList(server.listGames(authToken)));
        return "Games:\n" + formatGameList();
    }

    private String formatGameList() {
        var sb = new StringBuilder();
        sb.append("+--------+-------------+-----------------+-----------------+\n");
        sb.append(String.format("| %-6s | %-11s | %-15s | %-15s |%n", "ID", "Game Name", "White Player", "Black Player"));
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            sb.append(String.format("| %s%-6d %s| %-11s | %s%-15s %s| %s%-15s %s|%n",
                    SET_TEXT_COLOR_YELLOW, i + 1, SET_TEXT_COLOR_GREEN,
                    Optional.ofNullable(game.gameName()).orElse("N/A"),
                    SET_TEXT_COLOR_WHITE, Optional.ofNullable(game.whiteUsername()).orElse("none"),
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, Optional.ofNullable(game.blackUsername()).orElse("none"),
                    SET_TEXT_COLOR_GREEN));
            sb.append("+--------+-------------+-----------------+-----------------+\n");
        }
        return sb.append(SET_TEXT_COLOR_GREEN).toString();
    }

    public String join(String... args) throws RespExp {
        if (args.length < 2) throw new RespExp(400, "Expected: <ID> [WHITE|BLACK]");
        GameData game = validateGame(args[0]);
        String color = args[1].trim().toUpperCase();
        Set<String> colors = Set.of("WHITE", "BLACK");
        if (!colors.contains(color)) throw new RespExp(400, "Expected: [WHITE|BLACK]");

        if (("WHITE".equals(color) && !username.equals(game.whiteUsername())) ||
                ("BLACK".equals(color) && !username.equals(game.blackUsername()))) {
            server.joinGame(game.gameID(), color, authToken);
            return String.format("""
                    Successfully joined: %s. %s However, %s the Gameplay aspect has yet to be implemented.
                    Instead enjoy this lovely recreation of the board, from the perspective you would have in game.
                    %s
                    """,
                    game.gameName(),
                    SET_TEXT_ITALIC,
                    RESET_TEXT_ITALIC,
                    BoardPrint.print(ChessGame.TeamColor.valueOf(color), game.game().getBoard()));
        } else {
            return BoardPrint.print(ChessGame.TeamColor.valueOf(color), game.game().getBoard());
        }
    }

    private GameData validateGame(String id) throws RespExp {
        try {
            int index = Integer.parseInt(id) - 1;
            if (games.isEmpty()) throw new RespExp(400, "Please use list first");
            if (index < 0 || index >= games.size())
                throw new RespExp(400, "Invalid game ID, use list to verify");
            return games.get(index);
        } catch (NumberFormatException e) {
            throw new RespExp(400, "<ID> must be a number");
        }
    }

    public String observe(String... args) throws RespExp {
        if (args.length < 1) throw new RespExp(400, "Expected: <ID>");
        GameData game = validateGame(args[0]);
        return String.format("Observing: %s\n%s", game.gameName(), BoardPrint.print(game.game().getBoard()));
    }

    public String logout() throws RespExp {
        server.logout(authToken);
        return "transition ;; Logged out successfully.";
    }

    @Override
    public String help() {
        return String.format("""
                %s create <NAME> %s - Create a new game
                %s list %s - Display available games
                %s join <ID> [WHITE|BLACK] %s - Join an existing game
                %s observe <ID> %s - Watch an ongoing game
                %s logout %s - Log out from your account
                %s quit %s - Exit the chess client
                %s help %s - View available commands
                """,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA,
                SET_TEXT_COLOR_BLUE, SET_TEXT_COLOR_MAGENTA);
    }

    @Override
    public Client transition(String token) throws RespExp {
        int index = Integer.parseInt(token);
        GameData game = games.get(index);
        return new PlayClient(serverUrl, server, notifier, username, authToken, game);
    }

    @Override
    public Client transition() {
        return new PreLogClient(serverUrl, notifier);
    }

    @Override
    public String printState() {
        return "[LOGGED_IN]";
    }
}
