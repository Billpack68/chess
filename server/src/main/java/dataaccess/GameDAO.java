package dataaccess;

import com.google.gson.Gson;
import kotlin.NotImplementedError;
import model.AuthData;
import model.GameData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class GameDAO {

    private Gson gson;

    public GameDAO() throws ResponseException, DataAccessException {
        configureDatabase();
        gson = new Gson();
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


    public int addGameData(GameData newGameData) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?);";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            String whiteUsername = newGameData.whiteUsername();
            String blackUsername = newGameData.blackUsername();
            String gameName = newGameData.gameName();
            String gameJSON = gson.toJson(newGameData.game());

            preparedStatement.setString(1, whiteUsername);
            preparedStatement.setString(2, blackUsername);
            preparedStatement.setString(3, gameName);
            preparedStatement.setString(4, gameJSON);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new DataAccessException("Failed to insert game — no rows affected.");
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public int getNextGameID() throws DataAccessException, SQLException {
        String sql = "SELECT MAX(id) FROM games";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql);
             var rs = preparedStatement.executeQuery()) {

            int maxId = 0;
            if (rs.next()) {
                maxId = rs.getInt(1);
            }
            return maxId + 1;
        }
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

    public void deleteGameData() throws DataAccessException {
        System.out.println("Deleting game database");
        String sql = "DELETE FROM games;";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear games table", e);
        }
    }
}
