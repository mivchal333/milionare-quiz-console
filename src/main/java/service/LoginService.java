package service;

import api.MillionaireApi;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class LoginService {

    public Optional<User> attemptLogin(String username, String password) {
        User user = new User(username, password);
        MillionaireApi api = MillionaireApi.getApi();
        try {
            return api.sendLoginRequest(user);
        } catch (IOException e) {
            log.error("Request fails", e);
            return Optional.empty();
        }
    }

    public boolean attemptRegister(String username, String password, String nick) {
        User user = new User(username, password, nick);
        int responseCode = 0;
        MillionaireApi api = MillionaireApi.getApi();
        try {
            responseCode = api.sendRegisterRequest(user);
        } catch (IOException e) {
            log.error("Request fails", e);
            e.printStackTrace();
        }
        return responseCode == HttpStatus.SC_OK;
    }
}
