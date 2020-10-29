import model.User;
import org.apache.http.HttpStatus;

import java.io.IOException;

public class LoginService {

    public boolean attemptLogin(String username, String password) {
        User user = new User(username, password, null);
        int responseCode = 0;
        MillionaireApi api = MillionaireApi.getApi();
        try {
            responseCode = api.sendLoginRequest(user);
            System.out.println(responseCode);
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return responseCode == HttpStatus.SC_OK;
    }
}
