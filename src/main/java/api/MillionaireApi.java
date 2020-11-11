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

    public Optional<User> sendLoginRequest(User user) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(user);

        HttpURLConnection con = getHttpURLConnection("/login", RequestType.POST);


        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            response = new StringBuilder();
            String responseLine;
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

    public List<Question> fetchQuestions() {
        List<Question> questions;
        try {
            HttpURLConnection con = getHttpURLConnection("/question", RequestType.GET);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();

            questions = objectMapper.readValue(content.toString(), new TypeReference<List<Question>>() {
            });

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return questions;

    }

    public int sendRegisterRequest(User user) throws IOException {
        return sendPostRequest("/register", user.toJson());
    }

    public void saveAttempt(AttemptEntry entry) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(entry);

        HttpURLConnection con = getHttpURLConnection("/stats", RequestType.POST);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new IOException("Stats save error");
    }

    public List<AttemptEntry> fetchAttemptEntries(String username) throws IOException {

        HttpURLConnection con = getHttpURLConnection("/stats?username=" + username, RequestType.GET);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(content.toString(), new TypeReference<List<AttemptEntry>>() {
        });
    }

    private HttpURLConnection getHttpURLConnection(String path, RequestType requestType) throws IOException {
        URL url = new URL(BASE_API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestType.toString());
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        return con;
    }

    enum RequestType {GET, POST}
}
