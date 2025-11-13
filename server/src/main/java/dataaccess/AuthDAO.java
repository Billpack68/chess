package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class AuthDAO {

    public AuthDAO() throws DataAccessException {
        DatabaseGenerator.generateTables();
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
