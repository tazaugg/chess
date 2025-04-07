package websocket.commands;

public class Connect extends UserGameCommand {

    public Connect(String token, int id) {
        super(CommandType.CONNECT, token, id);
    }
}
