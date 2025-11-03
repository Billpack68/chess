package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
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


    public UserData addUserData(UserData newUserData) throws DataAccessException, SQLException {
        return null;
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var preparedStatement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
//                String username = newUserData.username();
//                String hashedPassword = BCrypt.hashpw(newUserData.password(), BCrypt.gensalt());
//                String email = newUserData.email();
//                int id = executeUpdate()
//                System.out.println(rs.getInt(1));
//            }
//        }
    }

    public void deleteUserData() {
        return;
    }

    public UserData getUser(String username) {
        return null;
    }
}
