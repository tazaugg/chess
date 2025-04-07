package websocket.commands;

public class LeaveCommand extends UserGameCommand {

    public LeaveCommand(String tokenValue, int gameIdValue) {
        super(CommandType.LEAVE, tokenValue, gameIdValue);
    }
}
