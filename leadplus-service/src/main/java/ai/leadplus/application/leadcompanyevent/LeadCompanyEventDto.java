package ai.leadplus.application.leadcompanyevent;

import ai.leadplus.domain.leadcompanyevents.EventType;
import ai.leadplus.domain.leadcompanyevents.LeadCompanyEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadCompanyEventDto {
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
    private boolean active;

    public static LeadCompanyEventDto toDto(LeadCompanyEvent event) {
        if (event == null) {
            return null;
        }
        return LeadCompanyEventDto.builder()
                .id(event.getId())
                .leadCompanyId(event.getLeadCompanyId())
                .type(event.getType())
                .title(event.getTitle())
                .summary(event.getSummary())
                .url(event.getUrl())
                .source(event.getSource())
                .sentiment(event.getSentiment())
                .publishedAt(event.getPublishedAt())
                .detectedAt(event.getDetectedAt())
                .uniqueHash(event.getUniqueHash())
                .active(event.isActive())
                .build();
    }

    public LeadCompanyEvent toEntity() {
        return LeadCompanyEvent.builder()
                .id(id)
                .leadCompanyId(leadCompanyId)
                .type(type)
                .title(title)
                .summary(summary)
                .url(url)
                .source(source)
                .sentiment(sentiment)
                .publishedAt(publishedAt)
                .detectedAt(detectedAt)
                .uniqueHash(uniqueHash)
                .active(active)
                .build();
    }

}
