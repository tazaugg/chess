package chess.pieces;

import chess.*;

import java.util.*;

public class Pawn implements ChessPieceMoves {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> validMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(position);
        int moveDirection = 0;
        boolean isStartingPosition = false;

        // Determine direction and starting position based on piece color
        if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            moveDirection = -1;
            isStartingPosition = position.getRow() == 7;
        } else if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            moveDirection = 1;
            isStartingPosition = position.getRow() == 2;
        }

        // Gather all valid moves for the pawn
        validMoves.addAll(generateForwardMoves(board, position, moveDirection, isStartingPosition));
        validMoves.addAll(generateCaptureMoves(board, position, moveDirection, piece));

        return validMoves;
    }

    private Collection<ChessMove> generatePromotionMoves(ChessPosition start, ChessPosition end) {
        List<ChessMove> promotionMoves = new ArrayList<>();
        ChessPiece.PieceType[] promotionTypes = {ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT};

        for (ChessPiece.PieceType type : promotionTypes) {
            promotionMoves.add(new ChessMove(start, end, type));
        }
        return promotionMoves;
    }

    private Collection<ChessMove> generateForwardMoves(ChessBoard board, ChessPosition position, int direction, boolean isStartingPosition) {
        List<ChessMove> forwardMoves = new ArrayList<>();
        int newRow = position.getRow() + direction;
        int column = position.getColumn();
        ChessPosition nextPosition = new ChessPosition(newRow, column);

        // Check if the move is within the board's boundaries
        if (isValidPosition(newRow, column)) {
            ChessPiece nextPiece = board.getPiece(nextPosition);
            // If no piece is blocking the forward path
            if (nextPiece == null) {
                // If reaching the promotion row
                if (newRow == 1 || newRow == 8) {
                    forwardMoves.addAll(generatePromotionMoves(position, nextPosition));
                } else {
                    forwardMoves.add(new ChessMove(position, nextPosition, null));
                    if (isStartingPosition) {
                        forwardMoves.addAll(generateForwardMoves(board, position, direction * 2, false)); // Double step on first move
                    }
                }
            }
        }
        return forwardMoves;
    }

    private Collection<ChessMove> generateCaptureMoves(ChessBoard board, ChessPosition position, int direction, ChessPiece piece) {
        List<ChessMove> captureMoves = new ArrayList<>();
        int newRow = position.getRow() + direction;

        // Check diagonals for capturing opponent pieces
        for (int i = -1; i <= 1; i += 2) {
            int newColumn = position.getColumn() + i;


            // Validate the move and check if there's an opponent piece to capture
            if (isValidPosition(newRow, newColumn)) {
                ChessPosition nextPosition = new ChessPosition(newRow, newColumn);
                ChessPiece targetPiece = board.getPiece(nextPosition);
                if (targetPiece != null && targetPiece.getTeamColor() != piece.getTeamColor()) {
                    if (newRow == 1 || newRow == 8) {
                        captureMoves.addAll(generatePromotionMoves(position, nextPosition));
                    } else {
                        captureMoves.add(new ChessMove(position, nextPosition, null));
                    }
                }
            }
        }
        return captureMoves;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
