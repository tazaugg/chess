package websocket.commands;

import chess.ChessMove;

public class MakeMove extends UserGameCommand {
    private final ChessMove chosenMove;

    public MakeMove(String token, int gameIdentifier, ChessMove chosenMove) {
        super(CommandType.MAKE_MOVE, token, gameIdentifier);
        this.chosenMove = chosenMove;
    }

    public ChessMove retrieveMove() {
        return this.chosenMove;
    }
}
