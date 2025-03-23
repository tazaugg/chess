package ui;

import chess.ChessGame;
import exceptions.RespExp;
import model.GameData;
import client.ServerFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class PostLogClient implements Client {
    private final String serverUrl;
    private final ServerFacade server;
    private final String authToken;
    private final String username;
    private final List<GameData> games;

    public PostLogClient(String serverUrl, ServerFacade server, String username, String authToken) {
        this.serverUrl = serverUrl;
        this.server = server;
        this.authToken = authToken;
        this.username = username;
        this.games = new ArrayList<>();
    }

    @Override
    public String eval(String input) {
        try {
            var tokens = input.trim().toLowerCase().split(" ");
            var command = (tokens.length > 0) ? tokens[0] : "help";
            var args = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (command) {
                case "create" -> createGame(args);
                case "list" -> listGames();
                case "join" -> joinGame(args);
                case "observe" -> observeGame(args);
                case "logout" -> logout();
                case "quit" -> exit();
                default -> help();
            };
        } catch (RespExp ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage();
        }
    }

    public String exit() {
        try {
            server.logout(authToken);
        } catch (RespExp ignored) {}
        return "quit";
    }

    public String createGame(String... args) throws RespExp {
        if (args.length < 1) {
            throw new RespExp(400, "Usage: create <GAME_NAME>");
        }
        server.createGame(args[0], authToken);
        return "Game successfully created!";
    }

    public String listGames() throws RespExp {
        games.clear();
        games.addAll(List.of(server.listGames(authToken)));
        return formatGameList();
    }

    private String formatGameList() {
        var output = new StringBuilder();
        output.append("+--------+-------------+-----------------+-----------------+\n");
        output.append(String.format("| %-6s | %-11s | %-15s | %-15s |%n",
                "ID", "Game Name", "White Player", "Black Player"));

        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            output.append(String.format("| %s%-6d %s| %-11s | %s%-15s %s| %s%-15s %s|%n",
                    SET_TEXT_COLOR_YELLOW, i + 1, SET_TEXT_COLOR_GREEN,
                    Optional.ofNullable(game.gameName()).orElse("N/A"),
                    SET_TEXT_COLOR_WHITE, Optional.ofNullable(game.whiteUsername()).orElse("None"),
                    SET_TEXT_COLOR_GREEN, SET_TEXT_COLOR_BLUE, Optional.ofNullable(game.blackUsername()).orElse("None"),
                    SET_TEXT_COLOR_GREEN
            ));
            output.append("+--------+-------------+-----------------+-----------------+\n");
        }
        return output.append(SET_TEXT_COLOR_GREEN).toString();
    }

    public String joinGame(String... args) throws RespExp {
        if (args.length < 2) {
            throw new RespExp(400, "Usage: join <ID> [WHITE|BLACK]");
        }
        GameData game = validateGame(args[0]);
        String color = args[1].trim().toUpperCase();

        if (!Set.of("WHITE", "BLACK").contains(color)) {
            throw new RespExp(400, "Valid colors: WHITE or BLACK");
        }

        if ((color.equals("WHITE") && username.equals(game.whiteUsername())) ||
                (color.equals("BLACK") && username.equals(game.blackUsername()))) {
            return BoardPrint.print(ChessGame.TeamColor.valueOf(color), game.game().getBoard());
        }

        server.joinGame(game.gameID(), color, authToken);
        return String.format("Joined %s as %s.%n%s",
                game.gameName(), color.toLowerCase(),
                BoardPrint.print(ChessGame.TeamColor.valueOf(color), game.game().getBoard()));
    }

    private GameData validateGame(String gameIdStr) throws RespExp {
        try {
            int gameIndex = Integer.parseInt(gameIdStr) - 1;

            if (games.isEmpty()) {
                throw new RespExp(400, "No games available. Use 'list' first.");
            }
            if (gameIndex < 0 || gameIndex >= games.size()) {
                throw new RespExp(400, "Invalid game ID. Check 'list' for available games.");
            }
            return games.get(gameIndex);
        } catch (NumberFormatException e) {
            throw new RespExp(400, "Game ID must be a number.");
        }
    }

    public String observeGame(String... args) throws RespExp {
        if (args.length < 1) {
            throw new RespExp(400, "Usage: observe <ID>");
        }
        GameData game = validateGame(args[0]);
        return String.format("Observing: %s%n%s", game.gameName(), BoardPrint.print(game.game().getBoard()));
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
    public Client transition(String token) {
        int gameIndex = Integer.parseInt(token);
        GameData game = games.get(gameIndex);
        return new PlayClient(serverUrl, server, username, authToken, game);
    }

    @Override
    public Client transition() {
        return new PreLogClient(serverUrl, server);
    }

    @Override
    public String printState() {
        return "[LOGGED_IN]";
    }
}
