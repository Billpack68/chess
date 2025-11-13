package dataaccess;

import kotlin.NotImplementedError;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class UserDAO {
    public UserDAO() throws DataAccessException {
        DatabaseGenerator.generateTables();
    }

    public UserData addUserData(UserData newUserData) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            String username = newUserData.username();
            String hashedPassword = BCrypt.hashpw(newUserData.password(), BCrypt.gensalt());
            String email = newUserData.email();

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.setString(3, email);

            try {
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DataAccessException("Error: Failed to insert user — no rows affected.");
                }

                return newUserData;
            } catch (SQLException e) {
                throw new DataAccessException("Error: Username or email already taken");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to add user to table");
        }
    }

    public void deleteUserData() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: Unable to clear users table", e);
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String foundUsername = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String email = resultSet.getString("email");
                    return new UserData(foundUsername, password, email);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to add user to table");
        }
    }

    public UserData getUserByEmail(String email) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE email = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, email);

            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    String foundEmail = resultSet.getString("email");
                    return new UserData(username, password, foundEmail);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to add user to table");
        }
    }
}
