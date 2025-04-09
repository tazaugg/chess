package client.websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;

public interface NotifHandler {

    void handleNotif(NotificationMessage note);
    void handleWarning(ErrorMessage err);
    void loadGame(LoadGameMessage game);
}
