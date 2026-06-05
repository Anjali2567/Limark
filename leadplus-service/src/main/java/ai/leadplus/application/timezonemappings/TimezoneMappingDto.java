package ai.leadplus.application.timezonemappings;

import ai.leadplus.domain.timezonemappings.LocationType;
import ai.leadplus.domain.timezonemappings.TimezoneMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimezoneMappingDto {

    private Long id;
    private LocationType locationType;
    private String locationKey;
    private String timezone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TimezoneMappingDto fromEntity(TimezoneMapping entity) {
        if (entity == null) return null;
        return TimezoneMappingDto.builder()
                .id(entity.getId())
                .locationType(entity.getLocationType())
                .locationKey(entity.getLocationKey())
                .timezone(entity.getTimezone())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
