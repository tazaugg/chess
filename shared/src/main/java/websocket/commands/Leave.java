package websocket.commands;

public class Leave extends UserGameCommand {

    public Leave(String tokenValue, int gameIdValue) {
        super(CommandType.LEAVE, tokenValue, gameIdValue);
    }
}
