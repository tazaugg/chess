package websocket.messages;

public class ErrorMessage extends ServerMessage {
    private final String messageContent;

    public ErrorMessage(String messageContent) {
        super(ServerMessageType.ERROR);
        this.messageContent = messageContent;
    }

    public String retrieveError() {
        if (messageContent.toLowerCase().contains("error")) {
            return messageContent;
        } else {
            return "Error: " + messageContent;
        }
    }
}
