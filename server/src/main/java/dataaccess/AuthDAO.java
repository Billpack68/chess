package dataaccess;

import model.AuthData;

import java.sql.Connection;
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
            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }


    public AuthData addAuthData(AuthData newAuthData) {
        return null;
    }

    public AuthData findAuthDataByAuthToken(String authToken) {
        return null;
    }

    public void deleteAuthDataByAuthToken(String authToken) {
        return;
    }

    public void deleteAuthData() {
        return;
    }
}
