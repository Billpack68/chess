package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import model.GameData;

import java.util.Collection;
import java.util.Objects;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameData findGameDataByID(int gameID) {
        GameData gameData = gameDAO.findGameDataByID(gameID);
        if (gameData == null) {
            throw new GameNotFoundException("Error: bad request");
        }
        return gameData;
    }

    public Collection<GameData> getGames() { return gameDAO.getGames(); }

    public void joinGame(GameData gameData, String teamColor, String username) {
        gameDAO.removeGameData(gameData);
        GameData newGameData;
        if (Objects.equals(teamColor, "WHITE")) {
            newGameData = new GameData(gameData.gameID(), username, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());
        } else {
            newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), username,
                    gameData.gameName(), gameData.game());
        }
        gameDAO.addGameData(newGameData);
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
