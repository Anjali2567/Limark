package ai.leadplus.api.v1.leadcompanyevents;

import ai.leadplus.application.leadcompanyevent.LeadCompanyEventDto;
import ai.leadplus.domain.leadcompanyevents.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCompanyEventRequest {

    @NotNull(message = "Type is mandatory")
    private EventType type;
    @NotBlank(message = "Title is mandatory")
    private String title;
    private String summary;
    private String url;
    private String source;
    private Integer sentiment;
    private LocalDateTime publishedAt;
    private LocalDateTime detectedAt;
    private String uniqueHash;

    public LeadCompanyEventDto toDto() {
        return LeadCompanyEventDto.builder()
                .type(type)
                .title(title)
                .summary(summary)
                .url(url)
                .source(source)
                .sentiment(sentiment)
                .publishedAt(publishedAt)
                .detectedAt(detectedAt)
                .uniqueHash(uniqueHash)
                .build();
    }
}
