package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    private final ChessGame currentGame;

    public LoadGameMessage(ChessGame currentGame) {
        super(ServerMessageType.LOAD_GAME);
        this.currentGame = currentGame;
    }

    public ChessGame fetchGame() {
        return this.currentGame;
    }
}
