package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameData findGameDataByID(int gameID) throws DataAccessException {
        GameData gameData = gameDAO.findGameDataByID(gameID);
        if (gameData == null) {
            throw new GameNotFoundException("Error: bad request");
        }
        return gameData;
    }

    public Collection<GameData> getGames() throws DataAccessException { return gameDAO.getGames(); }

    public void joinGame(GameData gameData, String teamColor, String username) throws DataAccessException {
        GameData newGameData;
        if (Objects.equals(teamColor, "WHITE")) {
            newGameData = new GameData(gameData.gameID(), username, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());
        } else {
            newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), username,
                    gameData.gameName(), gameData.game());
        }
        gameDAO.updateGame(gameData, newGameData);
    }

    public int createGame(String gameName) throws SQLException, DataAccessException {
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(null, null, null, gameName, newChessGame);
        return gameDAO.addGameData(newGame);
    }

    public void deleteGameData() throws DataAccessException {
        gameDAO.deleteGameData();
    }
}
