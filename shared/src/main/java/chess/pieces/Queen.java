package chess.pieces;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen implements ChessPieceMoves {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        //get all the moves that a Rook or a Bishop in the position of the Queen could do
        moves.addAll(new Bishop().pieceMoves(board, position));
        moves.addAll(new Rook().pieceMoves(board, position));

        return moves;
    }
}
