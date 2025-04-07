package websocket.commands;

public class ConnectCommand extends UserGameCommand {

    public ConnectCommand(String token, int id) {
        super(CommandType.CONNECT, token, id);
    }
}
