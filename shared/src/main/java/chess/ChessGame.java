package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTeam;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTeam = TeamColor.WHITE;


    }

    /**
     * @return Which team's turn it is
     */

    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null; // No piece at this position

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        List<ChessMove> legalMoves = new ArrayList<>();

        // Filter moves that do not leave the king in check
        for (ChessMove move : possibleMoves) {
            if (!wouldLeaveKingInCheck(piece.getTeamColor(), move)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;

    }
    private boolean wouldLeaveKingInCheck(TeamColor team, ChessMove move) {
        // Simulate the move on a temporary board
        ChessBoard tempBoard = copyBoard();
        tempBoard.addPiece(move.getEndPosition(), tempBoard.getPiece(move.getStartPosition()));
        tempBoard.addPiece(move.getStartPosition(), null);

        return isInCheck(team, tempBoard);
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            throw new InvalidMoveException("No piece at the start position.");
        }
        if (piece.getTeamColor() != currentTeam) {
            throw new InvalidMoveException("Piece is not in this team.");
        }
        Collection<ChessMove> legalMoves = validMoves(startPosition);
        if (legalMoves == null || legalMoves.isEmpty()) {
            throw new InvalidMoveException("No valid move.");
        }

        ChessBoard tempBoard = copyBoard();
        tempBoard.addPiece(endPosition, piece);
        tempBoard.addPiece(startPosition, null);

        if (isInCheck(currentTeam, tempBoard)) {
            throw new InvalidMoveException("Move puts player in check.");
        }
        if (piece.getPieceType()== ChessPiece.PieceType.PAWN){
            int startRow = startPosition.getRow();
            int startCol = startPosition.getColumn();
            int endRow = endPosition.getRow();
            int endCol = endPosition.getColumn();
            if (Math.abs(endRow-startRow)==2||Math.abs(endRow-startRow)==-2){
                if ((piece.getTeamColor()==TeamColor.WHITE && startRow !=2)||
                (piece.getTeamColor()==TeamColor.BLACK && startCol !=7)){
                    throw new InvalidMoveException("Pawn cannot move two after it has moved");
                }
            }
        }
        board.addPiece(endPosition, piece);
        board.addPiece(startPosition, null);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && (endPosition.getRow() == 1 || endPosition.getRow() == 8)) {
            ChessPiece.PieceType promotionType = move.getPromotionPiece();
            if (promotionType == null) {
                promotionType = ChessPiece.PieceType.QUEEN;
            }
            board.addPiece(endPosition, new ChessPiece(piece.getTeamColor(), promotionType));
        }
        currentTeam = (currentTeam == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }
    private boolean isInCheck(TeamColor teamColor, ChessBoard boardState) {
        ChessPosition kingPosition = findKing(teamColor, boardState);
        if (kingPosition == null) return false; // Should never happen in a valid game

        // Check if any opponent's piece can attack the king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = boardState.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(boardState, pos)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // King is under attack
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }
    private ChessPosition findKing(TeamColor team, ChessBoard boardState) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = boardState.getPiece(pos);
                if (piece != null && piece.getTeamColor() == team && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }
    private ChessBoard copyBoard() {
        ChessBoard newBoard = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null) {
                    newBoard.addPiece(pos, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return newBoard;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null");
        }
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
