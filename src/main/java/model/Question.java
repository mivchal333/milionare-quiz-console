package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {
    private Long id;

    private String question;

    private String correctAnswer;

    private List<String> incorrectAnswers;

}
