package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import kotlin.NotImplementedError;
import model.AuthData;
import model.GameData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
            throw new ResponseException(ResponseException.Code.ServerError,
                    String.format("Error: Unable to configure database: %s", ex.getMessage()));
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
                throw new DataAccessException("Error: Failed to insert game — no rows affected.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to add game to database");
        }

        String sql2 = "SELECT MAX(id) FROM games";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql2);
             var rs = preparedStatement.executeQuery()) {

            int maxId = 0;
            if (rs.next()) {
                maxId = rs.getInt(1);
            }
            return maxId;
        } catch (SQLException e) {
            throw new DataAccessException("Error: Couldn't connect", e);
        }
    }

    public GameData findGameDataByID(int gameID) throws DataAccessException {
        String sql = "SELECT id, whiteUsername, blackUsername, gameName, game FROM games WHERE id = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, gameID);

            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int foundID = resultSet.getInt("id");
                String whiteUsername = resultSet.getString("whiteUsername");
                String blackUsername = resultSet.getString("blackUsername");
                String gameName = resultSet.getString("gameName");
                ChessGame game = gson.fromJson(resultSet.getString("game"), ChessGame.class);
                return new GameData(foundID, whiteUsername, blackUsername, gameName, game);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: Unable to find game", e);
        }
    }

    public Collection<GameData> getGames() throws DataAccessException {
        String sql = "SELECT * FROM games";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            var resultSet = preparedStatement.executeQuery();
            Collection<GameData> results = new ArrayList<>();
            while (resultSet.next()) {
                int foundID = resultSet.getInt("id");
                String whiteUsername = resultSet.getString("whiteUsername");
                String blackUsername = resultSet.getString("blackUsername");
                String gameName = resultSet.getString("gameName");
                ChessGame game = gson.fromJson(resultSet.getString("game"), ChessGame.class);
                results.add(new GameData(foundID, whiteUsername, blackUsername, gameName, game));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Error: Unable to find game", e);
        }
    }

    public void updateGame(GameData oldGameData, GameData newGameData) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, game = ? WHERE id = ?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, newGameData.whiteUsername());
            preparedStatement.setString(2, newGameData.blackUsername());
            preparedStatement.setString(3, gson.toJson(newGameData.game()));
            preparedStatement.setInt(4, newGameData.gameID());
            preparedStatement.executeUpdate();

        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: Unable to update game", e);
        }
    }

    public void deleteGameData() throws DataAccessException {
        String sql = "DELETE FROM games;";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: Unable to clear games table", e);
        }
    }
}
