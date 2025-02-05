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
    private ChessMove lastMove;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTeam = TeamColor.WHITE;
        lastMove = null;


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
        if (piece == null) return null;

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        List<ChessMove> legalMoves = new ArrayList<>();

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
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("No valid move.");
        }

        ChessBoard tempBoard = copyBoard();
        tempBoard.addPiece(endPosition, piece);
        tempBoard.addPiece(startPosition, null);

        if (isInCheck(currentTeam, tempBoard)) {
            throw new InvalidMoveException("Move puts player in check.");
        }
        if (isCastlingMove(piece, move)) {
            performCastling(move);
        } else if (isEnPassantMove(piece, move)) {
            performEnPassant(move);
        } else {
            board.addPiece(endPosition, piece);
            board.addPiece(startPosition, null);
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN && (endPosition.getRow() == 1 || endPosition.getRow() == 8)) {
                board.addPiece(endPosition, new ChessPiece(piece.getTeamColor(), move.getPromotionPiece() != null ? move.getPromotionPiece() : ChessPiece.PieceType.QUEEN));
            }
        }
        lastMove = move;
        currentTeam = (currentTeam == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        movedPieces.add(startPosition);

    }

    private Set<ChessPosition> movedPieces = new HashSet<>();
    private boolean hasMoved(ChessPosition position) {
        return movedPieces.contains(position);
    }

    private boolean isCastlingMove(ChessPiece piece, ChessMove move) {
        if (piece.getPieceType() != ChessPiece.PieceType.KING ||
                Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) != 2){
            return false;
        }
        int row = move.getStartPosition().getRow();
        int col = move.getStartPosition().getColumn();
        int rookCol = (col == 7)  ? 8 : 1;
        ChessPosition rookPos = new ChessPosition(row, rookCol);
        ChessPiece rook = board.getPiece(rookPos);
        return rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK &&
                !hasMoved(rookPos) && !isInCheck(currentTeam) && isValidCastlingPath(row,rookCol) ;
    }
    private boolean isValidCastlingPath(int row, int rookCol) {
        int step = (rookCol == 8) ? 1 : -1;
        for (int col = 5; col != rookCol; col += step) {
            if (board.getPiece(new ChessPosition(row, col)) != null || wouldLeaveKingInCheck(currentTeam, new ChessMove(new ChessPosition(row, 5), new ChessPosition(row, col), null))) {
                return false;
            }
        }
        return true;
    }
    private void performCastling(ChessMove move) {
        int row = move.getStartPosition().getRow();
        int kingCol = move.getEndPosition().getColumn();
        int rookCol = (kingCol == 7) ? 8 : 1;
        int newRookCol = (kingCol == 7) ? 6 : 4;

        ChessPiece king = board.getPiece(move.getStartPosition());
        ChessPiece rook = board.getPiece(new ChessPosition(row, rookCol));

        board.addPiece(new ChessPosition(row, 5), null);
        board.addPiece(new ChessPosition(row, kingCol), king);
        board.addPiece(new ChessPosition(row, rookCol), null);
        board.addPiece(new ChessPosition(row, newRookCol), rook);
    }
    private boolean isEnPassantMove(ChessPiece piece, ChessMove move) {
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN || lastMove == null) {
            return false;
        }

        ChessPosition lastStart = lastMove.getStartPosition();
        ChessPosition lastEnd = lastMove.getEndPosition();
        ChessPiece lastPiece = board.getPiece(lastEnd);

        return lastPiece != null && lastPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                Math.abs(lastStart.getRow() - lastEnd.getRow()) == 2 &&
                move.getStartPosition().getColumn() == lastEnd.getColumn() &&
                move.getEndPosition().getRow() == lastStart.getRow() + ((piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1);
    }

    private void performEnPassant(ChessMove move) {
        ChessPosition capturedPawnPos = new ChessPosition(lastMove.getEndPosition().getRow(), lastMove.getEndPosition().getColumn());
        board.addPiece(capturedPawnPos, null);
        board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        board.addPiece(move.getStartPosition(), null);
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
        if (kingPosition == null) return false;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = boardState.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(boardState, pos)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
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
    public boolean canMove(ChessBoard boardState, TeamColor teamColor) {
        List<ChessMove> valid = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = boardState.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {

                    valid.addAll(validMoves(pos));
                }
            }
        }
        return !valid.isEmpty();
    }
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !canMove(board, teamColor);
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
        return !isInCheck(teamColor) && !canMove(board, teamColor);

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTeam == chessGame.currentTeam && Objects.equals(lastMove, chessGame.lastMove) && Objects.equals(movedPieces, chessGame.movedPieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTeam, lastMove, movedPieces);
    }
}
