package chess;
import java.util.Collection;


public interface ChessPieceMoves {
    public Collection<ChessMove> pieceMoves(ChessBoard board,ChessPosition position);
}
