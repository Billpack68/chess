package service;

import chess.ChessGame;
import dataaccess.MemoryGameDAO;
import model.GameData;

public class GameService {
    private final MemoryGameDAO gameDAO;

    public GameService(MemoryGameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameData getGame(GameData gameData) {
        return gameDAO.findGameData(gameData);
    }

    public int createGame(String gameName) {
        int newGameID = gameDAO.getNextGameID();
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(newGameID, null, null, gameName, newChessGame);
        gameDAO.addGameData(newGame);
        return newGameID;
    }

    public void deleteGameData() {
        gameDAO.deleteGameData();
    }
}
