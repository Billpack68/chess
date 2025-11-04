package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MemoryGameDAO extends GameDAO {
    private final Collection<GameData> gameData;

    public MemoryGameDAO() throws ResponseException, DataAccessException {
        this.gameData = new HashSet<>();
    }

    public int addGameData(GameData newGameData) {
        int newID = getNextGameID();
        newGameData.setNew
        gameData.add(newGameData);
    }

    public int getNextGameID() {
        int max = 0;
        for (GameData data : gameData) {
            if (data.gameID() > max) {
                max = data.gameID();
            }
        }
        return max + 1;
    }

    public void removeGameData(GameData searchData) {
        gameData.remove(searchData);
    }

    public GameData findGameDataByID(int gameID) {
        for (GameData game : gameData) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    public Collection<GameData> getGames() { return gameData; }

    public void deleteGameData() {
        gameData.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemoryGameDAO that = (MemoryGameDAO) o;
        return Objects.equals(gameData, that.gameData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(gameData);
    }

    @Override
    public String toString() {
        return "MemoryGameDAO{" +
                "gameData=" + gameData +
                '}';
    }
}
