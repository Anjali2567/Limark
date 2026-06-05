package ai.leadplus.application.common;

import ai.leadplus.domain.common.LocationFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LocationFilterDto {
    private List<String> cities;
    private List<String> states;
    private List<String> countries;
    private List<String> regions;

    public static LocationFilterDto fromEntity(LocationFilter locationFilter) {
        return LocationFilterDto.builder()
                .cities(locationFilter.getCities())
                .states(locationFilter.getStates())
                .countries(locationFilter.getCountries())
                .regions(locationFilter.getRegions())
                .build();
    }

    public LocationFilter toEntity() {
        return LocationFilter.builder()
                .cities(cities)
                .states(states)
                .countries(countries)
                .regions(regions)
                .build();
    }

    public String toCleanedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (!CollectionUtils.isEmpty(cities)) {
            sb.append("Cities: ").append(String.join(", ", cities)).append("\n");
        }
        if (!CollectionUtils.isEmpty(states)) {
            sb.append("States: ").append(String.join(", ", states)).append("\n");
        }
        if (!CollectionUtils.isEmpty(countries)) {
            sb.append("Countries: ").append(String.join(", ", countries)).append("\n");
        }
        if (!CollectionUtils.isEmpty(regions)) {
            sb.append("Regions: ").append(String.join(", ", regions)).append("\n");
        }
        sb.append("}");
        return sb.toString().trim();
    }
}
