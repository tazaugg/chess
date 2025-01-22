package chess.pieces;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class King implements ChessPieceMoves {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        // All possible relative moves for the king
        int[][] directions = {
                {1, 0},   // Move down
                {-1, 0},  // Move up
                {0, 1},   // Move right
                {0, -1},  // Move left
                {1, 1},   // Move down-right
                {1, -1},  // Move down-left
                {-1, 1},  // Move up-right
                {-1, -1}  // Move up-left
        };

        for (int[] direction : directions) {
            int newRow = position.getRow() + direction[0];
            int newCol = position.getColumn() + direction[1];

            // Check if the move is within bounds
            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);

                // Add move if the square is empty or contains an opponent's piece
                if (targetPiece == null || targetPiece.getTeamColor() != board.getPiece(position).getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }
}
