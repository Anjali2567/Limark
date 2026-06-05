package ai.leadplus.application.leadcontactevents;

import ai.leadplus.domain.leadcontactevents.LeadContactEventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadContactEventCountDto {

    private LeadContactEventCategory category;
    private long count;

    public static LeadContactEventCountDto of(LeadContactEventCategory category, long count) {
        return LeadContactEventCountDto.builder()
                .category(category)
                .count(count)
                .build();
    }
}

