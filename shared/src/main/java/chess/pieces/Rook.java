package chess.pieces;
import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Rook implements ChessPieceMoves {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        // Direction vectors for the rook (horizontal and vertical directions)
        int[][] directions = {
                {1, 0},  // Down
                {-1, 0}, // Up
                {0, 1},  // Right
                {0, -1}  // Left
        };

        // Loop over each direction to add valid moves
        for (int[] direction : directions) {
            int row = position.getRow();
            int col = position.getColumn();
            row += direction[0];
            col += direction[1];

            // Keep moving in the given direction
            while (!(row < 1 || row > 8 || col < 1 || col > 8)) {

                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null) {
                    moves.add(new ChessMove(position, newPosition, null)); // Empty square, add move
                } else if (targetPiece.getTeamColor() != board.getPiece(position).getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null)); // Capture opponent piece
                    break; // Can only capture one piece, can't move past
                } else {
                    break; // Blocked by friendly piece
                }

                row += direction[0];
                col += direction[1];
            }
        }
        return moves;
    }
}
