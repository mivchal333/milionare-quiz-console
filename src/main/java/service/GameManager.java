package service;

import api.MillionaireApi;
import lombok.extern.slf4j.Slf4j;
import model.AttemptEntry;
import model.Question;
import model.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GameManager {

    public List<Question> getQuestions() {
        MillionaireApi api = MillionaireApi.getApi();
        return api.fetchQuestions();
    }

    public void postNewQuestion(String questionContent, String currentAnswer, String incorrectAnswer1, String incorrectAnswer2, String incorrectAnswer3) {
        List<String> incorrectAnswers = Arrays.asList(incorrectAnswer1, incorrectAnswer2, incorrectAnswer3);
        MillionaireApi api = MillionaireApi.getApi();
        Question question = Question.builder()
                .question(questionContent)
                .correctAnswer(currentAnswer)
                .incorrectAnswers(incorrectAnswers)
                .build();
        try {
            api.postQuestion(question);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
