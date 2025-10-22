package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MemoryGameDAO {
    private final Collection<GameData> gameData;

    public MemoryGameDAO() {
        this.gameData = new HashSet<>();
    }

    public void addGameData(GameData newGameData) {
        gameData.add(newGameData);
    }

    public int getNextGameID() {
        return gameData.size() + 1;
    }

    public GameData findGameData(GameData searchData) {
        if (gameData.contains(searchData)) {
            return searchData;
        } else {
            return null;
        }
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
