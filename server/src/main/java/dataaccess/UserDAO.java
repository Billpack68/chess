package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.util.Objects;

public class UserDAO {
    public UserDAO() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS `users` (
            `id` INT NOT NULL AUTO_INCREMENT,
            `username` VARCHAR(100) NOT NULL,
            `password` VARCHAR(256) NOT NULL,
            `email` VARCHAR(320) NOT NULL,
            PRIMARY KEY (`id`),
            UNIQUE KEY `username_UNIQUE` (`username`),
            UNIQUE KEY `email_UNIQUE` (`email`)
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


    public UserData addUserData(UserData newUserData) {
        return null;
    }

    public void deleteUserData() {
        return;
    }

    public UserData getUser(String username) {
        return null;
    }
}
