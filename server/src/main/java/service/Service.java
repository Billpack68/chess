package service;

import model.*;

public class Service {
    private final AuthService authService;
    private final GameService gameService;
    private final UserService userService;

    public Service(AuthService authService, GameService gameService, UserService userService) {
        this.authService = authService;
        this.gameService = gameService;
        this.userService = userService;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws AlreadyTakenException,
            MissingDataException{
        String username = registerRequest.username();
        String password = registerRequest.password();
        String email = registerRequest.email();
        if (username == null || password == null || email == null) {
            throw new MissingDataException("Register requires username, password, and email");
        }
        UserData userData = new UserData(username, password, email);
        userService.addUser(userData);
        AuthData userAuth = authService.createAuth(username);
        return new RegisterResult(userAuth.username(), userAuth.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws MissingDataException, InvalidCredentialsException {
        String username = loginRequest.username();
        String password = loginRequest.password();
        if (username == null || password == null) {
            throw new MissingDataException("Login requires username and password");
        }
        userService.loginUser(username, password);
        AuthData userAuth = authService.createAuth(username);
        return new LoginResult(userAuth.username(), userAuth.authToken());
    }

    public void clearDB() {
        userService.deleteUserData();
        authService.deleteAuthData();
        gameService.deleteGameData();
    }
}
