package dataaccess;

import kotlin.NotImplementedError;
import model.GameData;

import java.sql.Connection;
import java.util.Collection;

public class GameDAO {

    public GameDAO() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS `games` (
            `id` INT NOT NULL AUTO_INCREMENT,
            `whiteUsername` VARCHAR(100),
            `blackUsername` VARCHAR(100),
            `gameName` VARCHAR(256) NOT NULL,
            `game` TEXT NOT NULL,
            PRIMARY KEY (`id`),
            FOREIGN KEY (`whiteUsername`) REFERENCES `users`(`username`)
                ON DELETE SET NULL
                ON UPDATE CASCADE,
            FOREIGN KEY (`blackUsername`) REFERENCES `users`(`username`)
                ON DELETE SET NULL
                ON UPDATE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };


    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (java.sql.SQLException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }


    public void addGameData(GameData newGameData) {
        throw new NotImplementedError();
    }

    public int getNextGameID() {
        throw new NotImplementedError();
    }

    public void removeGameData(GameData searchData) {
        throw new NotImplementedError();
    }

    public GameData findGameDataByID(int gameID) {
        throw new NotImplementedError();
    }

    public Collection<GameData> getGames() {
        throw new NotImplementedError();
    }

    public void deleteGameData() {
        throw new NotImplementedError();
    }
}
