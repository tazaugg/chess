package websocket.commands;

public class ResignCommand extends UserGameCommand {

    public ResignCommand(String token, int gameNumber) {
        super(CommandType.RESIGN, token, gameNumber);
    }
}
