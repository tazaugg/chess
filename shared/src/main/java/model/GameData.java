package model;
import chess.ChessGame;
import com.google.gson.Gson;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public String toString(){
        return new Gson().toJson(this);
    }
    public GameData setGameID(int gameID) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}
