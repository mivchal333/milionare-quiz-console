package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttemptEntry {
    private String username;
    private String nick;
    private Integer prize;
    private Date date;
}
