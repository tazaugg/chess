package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;

    public MakeMoveCommand(String token, int gameIdentifier, ChessMove chosenMove) {
        super(CommandType.MAKE_MOVE, token, gameIdentifier);
        this.move = chosenMove;
    }

    public ChessMove retrieveMove() {
        return this.move;
    }
}
