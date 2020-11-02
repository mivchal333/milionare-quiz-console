package service;

import api.MillionaireApi;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.apache.http.HttpStatus;

import java.io.IOException;

@Slf4j
public class LoginService {

    public boolean attemptLogin(String username, String password) {
        User user = new User(username, password);
        int responseCode;
        MillionaireApi api = MillionaireApi.getApi();
        try {
            responseCode = api.sendLoginRequest(user);
        } catch (IOException e) {
            log.error("Request fails", e);
            return false;
        }
        return responseCode == HttpStatus.SC_OK;
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
