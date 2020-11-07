package service;

import api.MillionaireApi;
import lombok.extern.slf4j.Slf4j;
import model.AttemptEntry;
import model.Question;
import model.User;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GameManager {

    public List<Question> getQuestions() {
        MillionaireApi api = MillionaireApi.getApi();
        return api.fetchQuestions();
    }

    public void saveAttempt(User user, Integer prize) {
        MillionaireApi api = MillionaireApi.getApi();
        AttemptEntry attemptEntry = new AttemptEntry();
        attemptEntry.setUsername(user.getUsername());
        attemptEntry.setPrize(prize);
        try {
            api.saveAttempt(attemptEntry);
        } catch (IOException e) {
            log.error("Unable to save user attempt stats", e);
        }
    }

    public List<AttemptEntry> getUserStats(String username) {
        MillionaireApi api = MillionaireApi.getApi();

        try {
            return api.fetchAttemptEntries(username);
        } catch (IOException e) {
            log.error("Unable to fet user attempts", e);
            return Collections.emptyList();
        }
    }
}
