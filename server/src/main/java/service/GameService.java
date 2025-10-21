package service;

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

    public GameData createGame(GameData gameData) {
        gameDAO.addGameData(gameData);
        return gameData;
    }

    public void deleteGameData() {
        gameDAO.deleteGameData();
    }
}
