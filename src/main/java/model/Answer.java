package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Answer implements Serializable {
    private long id;
    private String content;
    private Date editDate;
}
