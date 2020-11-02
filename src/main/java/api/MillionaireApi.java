package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MillionaireApi {
    private static MillionaireApi api;
    private final String BASE_API_URL = "http://localhost:8080";

    private MillionaireApi() {
    }

    public static MillionaireApi getApi() {
        if (api == null) {
            api = new MillionaireApi();
        }
        return api;
    }

    private int sendPostRequest(String path, String jsonPayload) throws IOException {
        URL url = new URL(BASE_API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return con.getResponseCode();

    }

    public int sendLoginRequest(User user) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(user);
        return sendPostRequest("/login", user.toJson());
    }

    public int sendRegisterRequest(User user) throws IOException {
        return sendPostRequest("/register", user.toJson());
    }
}
