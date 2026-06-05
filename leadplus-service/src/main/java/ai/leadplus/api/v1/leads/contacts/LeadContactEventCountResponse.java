package ai.leadplus.api.v1.leads.contacts;

import ai.leadplus.application.leadcontactevents.LeadContactEventCountDto;
import ai.leadplus.domain.leadcontactevents.LeadContactEventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadContactEventCountResponse {

    private LeadContactEventCategory category;
    private long count;

    public static LeadContactEventCountResponse fromDto(LeadContactEventCountDto dto) {
        if (dto == null) return null;
        return LeadContactEventCountResponse.builder()
                .category(dto.getCategory())
                .count(dto.getCount())
                .build();
    }
}

