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
    private boolean gameOver = false;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTeam = TeamColor.WHITE;
        lastMove = null;


    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", currentTeam=" + currentTeam +
                ", lastMove=" + lastMove +
                ", movedPieces=" + movedPieces +
                '}';
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
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        List<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            if (!wouldLeaveKingInCheck(piece.getTeamColor(), move)) {
                legalMoves.add(move);
            }
        }
        if (piece.getPieceType()== ChessPiece.PieceType.KING && !isInCheck(piece.getTeamColor())&& !movedPieces.contains(startPosition)) {
            //check to see if the king can move left or right,
            int[] rookCols = {1, 8};
            for(int rookCol : rookCols){
                int row = startPosition.getRow();
                int col = startPosition.getColumn();
                ChessPosition rookPosition = new ChessPosition(row,rookCol);
                ChessPiece rook = board.getPiece(rookPosition);
                int kingStartRow = piece.getTeamColor() == TeamColor.WHITE ? 1 : 8;

                if(rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK  && piece.getTeamColor() == rook.getTeamColor() &&
                        !movedPieces.contains(rookPosition) &&
                        isValidCastlingPath(row, col, rookCol) && kingStartRow == row && col == 5) {
                    int kingCol = rookCol == 8 ? 7 : 3;
                    ChessPosition end = new ChessPosition(row, kingCol);
                    ChessMove move = new ChessMove(startPosition, end, null);
                    legalMoves.add(move);
                }
            }
        }

        int direction = piece.getTeamColor() == TeamColor.WHITE ? 1 : -1;

        int newRow = startPosition.getRow() + direction;

        // Check diagonals for capturing opponent pieces
        for (int i = -1; i <= 1; i += 2) {
            int newColumn = startPosition.getColumn() + i;


            // Validate the move and check if there's an opponent piece to capture
            if (isValidPosition(newRow, newColumn)) {
                ChessPosition nextPosition = new ChessPosition(newRow, newColumn);
                ChessMove enPassant = new ChessMove(startPosition, nextPosition, null);
                if (isEnPassantMove(piece, enPassant) && !wouldLeaveKingInCheck(currentTeam, enPassant)) {
                    legalMoves.add(enPassant);
                }
            }
        }
               return legalMoves;

    }

    private boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
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
            String correctTeam = (currentTeam == TeamColor.WHITE) ? "white" : "black";
            throw new InvalidMoveException("Error: It is the " + correctTeam + " team's turn.");
        }

        Collection<ChessMove> legalMoves = validMoves(startPosition);
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Error: From " + move.getStartPosition().prettyOutput()
                    + " to " + move.getEndPosition().prettyOutput() + " is not a valid move.");
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
                board.addPiece(endPosition, new ChessPiece(piece.getTeamColor(),
                        move.getPromotionPiece() != null ? move.getPromotionPiece() : ChessPiece.PieceType.QUEEN));
            }
        }
        lastMove = move;
        currentTeam = (currentTeam == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        movedPieces.add(startPosition);
        movedPieces.add(endPosition);

    }

    private final Set<ChessPosition> movedPieces = new HashSet<>();
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
        int kingCol= move.getStartPosition().getColumn();
        int rookCol = (col == 7)  ? 8 : 1;
        ChessPosition rookPos = new ChessPosition(row, rookCol);
        ChessPiece rook = board.getPiece(rookPos);
        if (rook == null || rook.getPieceType() != ChessPiece.PieceType.ROOK || hasMoved(rookPos)) {
            return false;
        }
        return isValidCastlingPath(row, kingCol, rookCol)&& !isInCheck(currentTeam);
    }
    private boolean isValidCastlingPath(int row,int kingCol, int rookCol) {
        ChessPosition kingPos = new ChessPosition(row,kingCol);
        ChessPiece king = board.getPiece(kingPos);
        if(king == null){
            return false;
        }

        int step = (rookCol == 8) ? 1 : -1;
        for (int col = kingCol + step; col != rookCol; col += step) {
            ChessPosition pos = new ChessPosition(row, col);
            if (board.getPiece(pos) != null || (Math.abs(kingCol - col) <= 2
                    && wouldLeaveKingInCheck(king.getTeamColor(), new ChessMove(new ChessPosition(row, kingCol), pos, null)))) {
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

        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), king);
        board.addPiece(new ChessPosition(row, rookCol), null);
        board.addPiece(new ChessPosition(row, newRookCol), rook);
        movedPieces.add(move.getStartPosition());
        movedPieces.add(move.getEndPosition());
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
                move.getStartPosition().getRow() == lastEnd.getRow() &&
                move.getEndPosition().getColumn() == lastStart.getColumn();
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
        if (kingPosition == null){
            return false;
        }
        List<ChessMove> opposingMoves = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = boardState.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    opposingMoves.addAll(piece.pieceMoves(boardState, pos));
                }
            }
        }
        Iterator<ChessMove> iterator = opposingMoves.iterator();
        while (iterator.hasNext()) {
            ChessMove move = iterator.next();
            if(move.getEndPosition().equals(kingPosition)){
                return true;
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
        movedPieces.clear();
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

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Ends or resumes the game.
     * @param gameOver whether the game should be marked as over
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTeam == chessGame.currentTeam &&
                Objects.equals(lastMove, chessGame.lastMove) && Objects.equals(movedPieces, chessGame.movedPieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTeam, lastMove, movedPieces);
    }
}
