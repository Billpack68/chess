package dataaccess;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import service.AuthService;
import service.MissingDataException;

import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    @BeforeEach
    void setUp() throws ResponseException, DataAccessException {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userDAO.deleteUserData();
        authDAO.deleteAuthData();
        gameDAO.deleteGameData();
    }

    //AddUser+
    @Test
    void TestAddUserPositive() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        UserData user = userDAO.getUser("username");
        assert(Objects.equals(newUser.username(), user.username()));
        assert(BCrypt.checkpw(newUser.password(), user.password()));
        assert(Objects.equals(newUser.email(), user.email()));
    }

    //AddUser-
    @Test
    void TestAddUserUsernameTaken() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        UserData sameUsername = new UserData("username", "password2", "email2");
        assertThrows(DataAccessException.class, () -> {
            userDAO.addUserData(sameUsername);
        });
    }

    //GetUser+
    @Test
    void TestGetUserPositive() throws SQLException, DataAccessException {
        UserData newUser = new UserData("fancyUsername", "password", "email");
        userDAO.addUserData(newUser);
        UserData user = userDAO.getUser("fancyUsername");
        assert(Objects.equals(newUser.username(), user.username()));
    }

    //GetUser-
    @Test
    void TestGetUserInvalidUsername() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        assertNull(userDAO.getUser("differentUsername"));
    }

    //DeleteUsers+
    @Test
    void TestDeleteUserData() throws SQLException, DataAccessException {
        UserData newUser = new UserData("fancyUsername", "password", "email");
        userDAO.addUserData(newUser);
        userDAO.deleteUserData();
        assertNull(userDAO.getUser("fancyUsername"));
    }

    @Test
    void TestAddAuthData() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);

        AuthService authService = new AuthService(authDAO);

        AuthData result = authService.createAuth("username");
        // Either it throws an error or it returns AuthData with a token and a username, I don't know what
        // the authToken is, but I can compare usernames!
        assert(Objects.equals(result.username(), "username"));
    }

    @Test
    void TestAddAuthDataMissingData() {
        AuthData badData = new AuthData(null, "username");
        assertThrows(DataAccessException.class, () -> {
            authDAO.addAuthData(badData);
        });
    }

    @Test
    void TestGetAuthData() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        AuthService authService = new AuthService(authDAO);
        AuthData result = authService.createAuth("username");
        assert(Objects.equals(result, authDAO.findAuthDataByAuthToken(result.authToken())));
    }

    @Test
    void TestGetAuthDataBad() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);
        AuthService authService = new AuthService(authDAO);
        AuthData result = authService.createAuth("username");
        assertNull(authDAO.findAuthDataByAuthToken("fakeAuth"));
    }

    @Test
    void TestDeleteAuthDataByAuthDataPositive() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);

        AuthService authService = new AuthService(authDAO);
        AuthData result = authService.createAuth("username");

        assertNotNull(authDAO.findAuthDataByAuthToken(result.authToken()));

        authDAO.deleteAuthDataByAuthToken(result.authToken());

        assertNull(authDAO.findAuthDataByAuthToken(result.authToken()));
    }

    @Test
    void TestDeleteAuthDataByAuthDataNegative() throws SQLException, DataAccessException {
        UserData newUser = new UserData("username", "password", "email");
        userDAO.addUserData(newUser);

        AuthService authService = new AuthService(authDAO);
        AuthData result = authService.createAuth("username");

        assertNotNull(authDAO.findAuthDataByAuthToken(result.authToken()));

        authDAO.deleteAuthDataByAuthToken("fakeAuthToken");

        assertNotNull(authDAO.findAuthDataByAuthToken(result.authToken()));
    }

    @Test
    void TestDeleteAllAuthData() throws SQLException, DataAccessException {
        UserData newUser1 = new UserData("username1", "password", "email1");
        UserData newUser2 = new UserData("username2", "password", "email2");
        UserData newUser3 = new UserData("username3", "password", "email3");
        UserData newUser4 = new UserData("username4", "password", "email4");
        userDAO.addUserData(newUser1);
        userDAO.addUserData(newUser2);
        userDAO.addUserData(newUser3);
        userDAO.addUserData(newUser4);

        AuthService authService = new AuthService(authDAO);
        AuthData result1 = authService.createAuth("username1");
        AuthData result2 = authService.createAuth("username2");
        AuthData result3 = authService.createAuth("username3");
        AuthData result4 = authService.createAuth("username4");

        assertNotNull(authDAO.findAuthDataByAuthToken(result1.authToken()));
        assertNotNull(authDAO.findAuthDataByAuthToken(result2.authToken()));
        assertNotNull(authDAO.findAuthDataByAuthToken(result3.authToken()));
        assertNotNull(authDAO.findAuthDataByAuthToken(result4.authToken()));

        authDAO.deleteAuthData();

        assertNull(authDAO.findAuthDataByAuthToken(result1.authToken()));
        assertNull(authDAO.findAuthDataByAuthToken(result2.authToken()));
        assertNull(authDAO.findAuthDataByAuthToken(result3.authToken()));
        assertNull(authDAO.findAuthDataByAuthToken(result4.authToken()));
    }

    @Test
    void testGameGsoning() throws InvalidMoveException {
        ChessGame original = new ChessGame();

        Gson gson = new Gson();
        String json = gson.toJson(original);
        ChessGame copy = gson.fromJson(json, ChessGame.class);
        assert(copy.equals(original));

        ChessMove firstMove = new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5),
                null);
        original.makeMove(firstMove);

        ChessMove secondMove = new ChessMove(new ChessPosition(7, 1), new ChessPosition(5, 1),
                null);
        original.makeMove(secondMove);

        ChessMove thirdMove = new ChessMove(new ChessPosition(4, 5), new ChessPosition(5, 5),
                null);
        original.makeMove(thirdMove);

        json = gson.toJson(original);
        copy = gson.fromJson(json, ChessGame.class);
        assert(copy.equals(original));

        ChessMove fourthMove = new ChessMove(new ChessPosition(7, 4), new ChessPosition(5, 4),
                null);
        original.makeMove(fourthMove);

        json = gson.toJson(original);
        copy = gson.fromJson(json, ChessGame.class);
        assert(copy.equals(original));
        assert(copy.getPossibleEnPassant().equals(original.getPossibleEnPassant()));

        ChessMove enPassant = new ChessMove(new ChessPosition(5,5), new ChessPosition(6,4),
                null);
        original.makeMove(enPassant);

        json = gson.toJson(original);
        copy = gson.fromJson(json, ChessGame.class);
        assert(copy.equals(original));
        assert(copy.getPossibleEnPassant().equals(original.getPossibleEnPassant()));
        System.out.println(original.getBoard());
        System.out.println(json);
    }

    @Test
    void TestAddGame() throws SQLException, DataAccessException {
        GameData newGameData = new GameData(null, null, null, "gameName",
                new ChessGame());
        GameData newGameData2 = new GameData(null, null, null, "gameName",
                new ChessGame());
        int gameID1 = gameDAO.addGameData(newGameData);
        int gameID2 = gameDAO.addGameData(newGameData2);
        assert(gameID2 > 1);
    }

    @Test
    void TestAddGameInvalidUsername() throws SQLException, DataAccessException {
        GameData newGameData = new GameData(null, "fakeUsername", null, "gameName",
                new ChessGame());
        assertThrows(DataAccessException.class, () -> {
            gameDAO.addGameData(newGameData);
        });
    }

    @Test
    void TestGetNextGameID() throws DataAccessException, SQLException {
        GameData newGameData = new GameData(null, null, null, "gameName",
                new ChessGame());
        GameData newGameData2 = new GameData(null, null, null, "gameName",
                new ChessGame());
        int gameID1 = gameDAO.addGameData(newGameData);
        int gameID2 = gameDAO.addGameData(newGameData2);
    }
}
