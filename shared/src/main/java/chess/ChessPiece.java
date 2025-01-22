package chess;

import chess.pieces.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor; // Initialize the piece's team color
        this.type = type; // Initialize the piece's type
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor; // Return the team color of the piece
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type; // Return the type of the piece
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> moves = new ArrayList<>();
        switch (piece.getPieceType()) {
            case KING -> {
            }
            case QUEEN ->
                moves.addAll(new Queen().pieceMoves(board,myPosition));
            case BISHOP->
                moves.addAll(new Bishop().pieceMoves(board,myPosition));



            case KNIGHT ->
                moves.addAll(new Knight().pieceMoves(board,myPosition));
            case ROOK->
                moves.addAll(new Rook().pieceMoves(board,myPosition));
            case PAWN -> {
            }
        }

        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}