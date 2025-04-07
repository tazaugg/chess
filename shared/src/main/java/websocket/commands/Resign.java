package websocket.commands;

public class Resign extends UserGameCommand {

    public Resign(String token, int gameNumber) {
        super(CommandType.RESIGN, token, gameNumber);
    }
}
