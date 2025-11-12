package ui;

import com.google.gson.Gson;
import model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.*;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(RegisterRequest request) {
        var httpRequest = buildRequest("POST", "/user", request);
        var httpResponse = sendRequest(httpRequest);
        return handleResponse(httpResponse, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) {
        return null;
    }

    public LogoutResult logout(LogoutRequest request) {
        return null;
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        return null;
    }

    public CreateGameResult createGame(CreateGameRequest request) {
        return null;
    }

    public JoinGameResult joinGame(JoinGameRequest request) {
        return null;
    }

    public ClearDatabaseResult clearDB(ClearDatabaseRequest request) {
        var httpRequest = buildRequest("DELETE", "/db", request);
        var httpResponse = sendRequest(httpRequest);
        return handleResponse(httpResponse, ClearDatabaseResult.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ServerFacadeException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ServerFacadeException("Error: unable to communicate with server");
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ServerFacadeException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw new ServerFacadeException("Error " + status);
            }

            throw new ServerFacadeException("Error " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
