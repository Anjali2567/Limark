package ai.leadplus.application.leadfileimport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {

    private String field;
    private String previousValue;
    private String newValue;

    public static void trackIfChanged(List<FieldChange> changes,
                                      String field,
                                      String previousValue,
                                      String newValue) {
        if (!java.util.Objects.equals(previousValue, newValue)) {
            changes.add(new FieldChange(field, previousValue, newValue));
        }
    }

    public static List<FieldChange> newList() {
        return new ArrayList<>();
    }
}