package chess.pieces;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight implements ChessPieceMoves {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        // The relative positions the knight can move to
        int[][] offsets = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        // Loop through each possible offset
        for (int[] offset : offsets) {
            int newRow = position.getRow() + offset[0];
            int newCol = position.getColumn() + offset[1];

            // Check if the position is within bounds
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);

                // If the square is empty or contains an opponent's piece, add the move
                if (targetPiece == null || targetPiece.getTeamColor() != board.getPiece(position).getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }
}
