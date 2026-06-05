package ai.leadplus.api.v1.leadcompanyevents;

import ai.leadplus.application.leadcompanyevent.LeadCompanyEventDto;
import ai.leadplus.domain.leadcompanyevents.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCompanyEventResponse {

    private Long id;
    private Long leadCompanyId;
    private EventType type;
    private String title;
    private String summary;
    private String url;
    private String source;
    private Integer sentiment;
    private LocalDateTime publishedAt;
    private LocalDateTime detectedAt;
    private String uniqueHash;

    public static LeadCompanyEventResponse toResponse(LeadCompanyEventDto dto) {
        if (dto == null) {
            return null;
        }
        return LeadCompanyEventResponse.builder()
                .id(dto.getId())
                .leadCompanyId(dto.getLeadCompanyId())
                .type(dto.getType())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .url(dto.getUrl())
                .source(dto.getSource())
                .sentiment(dto.getSentiment())
                .publishedAt(dto.getPublishedAt())
                .detectedAt(dto.getDetectedAt())
                .uniqueHash(dto.getUniqueHash())
                .build();
    }
}
