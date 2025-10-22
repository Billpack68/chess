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
        UserData existingUser = userService.getUserByUsername(username);
        if (existingUser != null) {
            throw new AlreadyTakenException("Username already exists");
        }
        UserData newUser = new UserData(username, password, email);
        userService.addUser(newUser);
        AuthData userAuth = authService.createAuth(username);
        authService.addAuth(userAuth);
        return new RegisterResult(userAuth.username(), userAuth.authToken());
    }

    public void clearDB() {
        userService.deleteUserData();
        authService.deleteAuthData();
        gameService.deleteGameData();
    }
}
