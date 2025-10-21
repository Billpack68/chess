package dataaccess;

import model.GameData;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MemoryGameDAO {
    private final Set<GameData> gameData;

    public MemoryGameDAO() {
        this.gameData = new HashSet<>();
    }

    public GameData addGameData(GameData newGameData) {
        gameData.add(newGameData);
        return newGameData;
    }

    public GameData findGameData(GameData searchData) {
        if (gameData.contains(searchData)) {
            return searchData;
        } else {
            return null;
        }
    }

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
