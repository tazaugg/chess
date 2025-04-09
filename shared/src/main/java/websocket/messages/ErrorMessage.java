package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;

    public ErrorMessage(String messageContent) {
        super(ServerMessageType.ERROR);
        this.errorMessage = messageContent;
    }

    public String retrieveError() {
        if (errorMessage.toLowerCase().contains("error")) {
            return errorMessage;
        } else {
            return "Error: " + errorMessage;
        }
    }
}
