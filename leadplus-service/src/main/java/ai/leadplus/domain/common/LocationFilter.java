package ai.leadplus.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LocationFilter {
    private List<String> cities;
    private List<String> states;
    private List<String> countries;
    private List<String> regions;
}
