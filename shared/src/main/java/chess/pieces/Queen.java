package chess.pieces;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen implements ChessPieceMoves {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();

        int[][] directions = {
                {1, 0},   // down
                {-1, 0},  // up
                {0, 1},   // right
                {0, -1},  // left
                {1, 1},   // bottom-right
                {1, -1},  // bottom-left
                {-1, 1},  // top-right
                {-1, -1}  // top-left
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
