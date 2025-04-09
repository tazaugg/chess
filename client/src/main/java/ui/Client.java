package ui;

import chess.ChessGame;
import exceptions.RespExp;

public interface Client {
    String help();
    Client transition(String token) throws RespExp;
    Client transition();
    String printState();
    String eval(String input);
    String loadGame(ChessGame game);
}
