package ui;

import chess.*;

import java.util.*;

import static ui.EscapeSequences.*;

public class BoardPrint {
    private static final String[] FILES = {
            EMPTY, " h\u2003", " g\u2003", " f\u2003", " e\u2003", " d\u2003", " c\u2003", " b\u2003", " a\u2003", EMPTY
    };

    private ChessGame game;

    public BoardPrint() {
        this.game = new ChessGame();
    }

    public BoardPrint(ChessGame game) {
        this.game = game;
    }

    public void updateGame(ChessGame game) {
        this.game = game;
    }

    public ChessBoard getBoard() {
        return this.game.getBoard();
    }

    public String print() {
        return print(ChessGame.TeamColor.WHITE);
    }

    public String print(ChessGame.TeamColor team) {
        return print(team, game.getBoard());
    }

    public String print(ChessGame.TeamColor team, ChessBoard board) {
        return print(team, board, null, Collections.emptyList());
    }

    public String highlightValidMoves(ChessGame.TeamColor team, ChessPosition selected) {
        Collection<ChessMove> possibleMoves = game.validMoves(selected);
        List<ChessPosition> highlights = new ArrayList<>();

        if (possibleMoves != null) {
            for (ChessMove move : possibleMoves) {
                highlights.add(move.getEndPosition());
            }
        }

        return print(team, game.getBoard(), selected, highlights);
    }

    private String print(ChessGame.TeamColor team, ChessBoard board,
                         ChessPosition selected, Collection<ChessPosition> highlights) {

        int direction = team == ChessGame.TeamColor.WHITE ? -1 : 1;
        int startEdge = team == ChessGame.TeamColor.WHITE ? 9 : 0;

        ChessMove lastMove = game.getLastMove();
        ChessPosition start = lastMove != null ? lastMove.getStartPosition() : null;
        ChessPosition end = lastMove != null ? lastMove.getEndPosition() : null;

        StringBuilder out = new StringBuilder();
        out.append(RESET).append(SET_TEXT_BOLD);

        for (int rank = startEdge; rank <= 9 && rank >= 0; rank += direction) {
            out.append("\n");
            for (int file = startEdge; file <= 9 && file >= 0; file += direction) {
                if (rank == 9 || rank == 0 || file == 9 || file == 0) {
                    out.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_MAGENTA);
                    out.append(rank == 9 || rank == 0 ? FILES[file] : " " + rank + "\u2003");
                } else {
                    ChessPosition pos = new ChessPosition(rank, 9 - file);

                    if (pos.equals(start) || pos.equals(end)) {
                        out.append(SET_BG_COLOR_MAGENTA);
                    } else if (pos.equals(selected)) {
                        out.append(SET_BG_COLOR_YELLOW);
                    } else if (highlights.contains(pos)) {
                        out.append(SET_BG_COLOR_GREEN);
                    } else if ((rank + file) % 2 == 0) {
                        out.append(SET_BG_COLOR_LIGHT_GREY);
                    } else {
                        out.append(SET_BG_COLOR_BLACK);
                    }

                    out.append(displayPiece(board.getPiece(pos)));
                }
            }
            out.append(RESET_BG_COLOR);
        }

        out.append(RESET);
        return out.toString();
    }

    private static String displayPiece(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    SET_TEXT_COLOR_WHITE + WHITE_KING : SET_TEXT_COLOR_BLUE + BLACK_KING;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    SET_TEXT_COLOR_WHITE + WHITE_PAWN : SET_TEXT_COLOR_BLUE + BLACK_PAWN;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    SET_TEXT_COLOR_WHITE + WHITE_ROOK : SET_TEXT_COLOR_BLUE + BLACK_ROOK;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    SET_TEXT_COLOR_WHITE + WHITE_BISHOP : SET_TEXT_COLOR_BLUE + BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    SET_TEXT_COLOR_WHITE + WHITE_KNIGHT : SET_TEXT_COLOR_BLUE + BLACK_KNIGHT;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    SET_TEXT_COLOR_WHITE + WHITE_QUEEN : SET_TEXT_COLOR_BLUE + BLACK_QUEEN;
        };
    }
}
