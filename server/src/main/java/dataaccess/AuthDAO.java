package dataaccess;

import kotlin.NotImplementedError;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class AuthDAO {

    public AuthDAO() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS `auths` (
            `id` INT NOT NULL AUTO_INCREMENT,
            `authToken` VARCHAR(256) NOT NULL,
            `username` VARCHAR(100),
            PRIMARY KEY (`id`),
            FOREIGN KEY (`username`) REFERENCES `users`(`username`)
                ON DELETE CASCADE
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
            throw new ResponseException(ResponseException.Code.ServerError, String.format("Error: Unable to configure database: %s", ex.getMessage()));
        }
    }


    public AuthData addAuthData(AuthData newAuthData) throws DataAccessException {
        String sql = "INSERT INTO auths (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            String authToken = newAuthData.authToken();
            String username = newAuthData.username();


            preparedStatement.setString(1, authToken);
            preparedStatement.setString(2, username);

            try {
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DataAccessException("Error: Unable to add authData");
                }

                return newAuthData;
            } catch (SQLException e) {
                throw new DataAccessException("Error: Unable to add authData (username not found): " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to add user to table");
        }
    }

    public AuthData findAuthDataByAuthToken(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auths WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, authToken);

            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String foundAuthToken = resultSet.getString("authToken");
                    String foundUsername = resultSet.getString("username");
                    return new AuthData(foundAuthToken, foundUsername);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to add user to table");
        }
    }

    public void deleteAuthDataByAuthToken(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auths WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();

        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: Unable to clear users table", e);
        }
    }

    public void deleteAuthData() throws DataAccessException {
        String sql = "DELETE FROM auths";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: Unable to clear users table", e);
        }
    }
}
