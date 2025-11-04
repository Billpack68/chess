package dataaccess;

import java.sql.Connection;

public class DatabaseGenerator {
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;

    public static void generateTables() throws DataAccessException {
        String[] authCreateStatements = {
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
        String[] gameCreateStatements = {
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
        String[] userCreateStatements = {
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
        String[][] statements = {userCreateStatements, authCreateStatements, gameCreateStatements};

        for (String[] statement : statements ) {
            configureDatabase(statement);
        }
    }

    private static void configureDatabase(String[] createStatements) throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("Error: Unable to configure database");
        }
    }

}
