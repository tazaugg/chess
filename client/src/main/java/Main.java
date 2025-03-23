import chess.*;
import ui.RepL;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if(args.length == 1){
            serverUrl = args[0];
        }
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        new RepL(serverUrl).run();
    }
}