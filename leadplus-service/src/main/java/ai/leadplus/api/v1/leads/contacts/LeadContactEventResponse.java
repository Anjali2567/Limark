package ai.leadplus.api.v1.leads.contacts;

import ai.leadplus.application.leadcontactevents.LeadContactEventDetailedDto;
import ai.leadplus.domain.leadcontactevents.LeadContactEventCategory;
import ai.leadplus.domain.leadcontactevents.LeadContactEventType;
import ai.leadplus.domain.leadcontactevents.LeadContactEventSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadContactEventResponse {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long contactId;
    private LeadContactEventCategory category;
    private LeadContactEventType type;
    private String description;
    private Long sourceId;
    private LeadContactEventSourceType sourceType;
    private String userName;
    private String eventBy;
    private LocalDateTime eventAt;
    private LocalDateTime createdAt;

    public static LeadContactEventResponse fromDto(LeadContactEventDetailedDto dto) {
        if (dto == null) return null;
        return LeadContactEventResponse.builder()
                .id(dto.getId())
                .tenantId(dto.getTenantId())
                .workspaceId(dto.getWorkspaceId())
                .contactId(dto.getContactId())
                .category(dto.getCategory())
                .type(dto.getType())
                .description(dto.getDescription())
                .sourceId(dto.getSourceId())
                .sourceType(dto.getSourceType())
                .userName(dto.getUserName())
                .eventBy(dto.getEventBy())
                .eventAt(dto.getEventAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
