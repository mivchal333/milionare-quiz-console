package api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.AttemptEntry;
import model.Question;
import model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private List<Question> sendGetRequest(String path) throws IOException {
        URL url = new URL(BASE_API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        ObjectMapper objectMapper = new ObjectMapper();

        List<Question> questions = objectMapper.readValue(content.toString(), new TypeReference<List<Question>>() {
        });
        return questions;
    }

    public Optional<User> sendLoginRequest(User user) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(user);

        URL url = new URL(BASE_API_URL + "/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        StringBuilder response;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        try {
            User user1 = objectMapper.readValue(response.toString(), User.class);
            return Optional.ofNullable(user1);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public int sendRegisterRequest(User user) throws IOException {
        return sendPostRequest("/register", user.toJson());
    }

    public List<Question> fetchQuestions() {
        List<Question> questions = null;
        try {
            questions = sendGetRequest("/question");
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return questions;

    }

    public void saveAttempt(AttemptEntry entry) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(entry);

        URL url = new URL(BASE_API_URL + "/stats");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new IOException("Stats save error");
    }
}
