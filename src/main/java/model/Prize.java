package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Prize {
    private int value;
    private boolean guaranteed;
}
