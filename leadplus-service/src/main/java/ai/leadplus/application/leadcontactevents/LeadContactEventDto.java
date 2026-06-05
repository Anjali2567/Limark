package ai.leadplus.application.leadcontactevents;

import ai.leadplus.domain.leadcontactevents.LeadContactEventCategory;
import ai.leadplus.domain.leadcontactevents.LeadContactEventType;
import ai.leadplus.domain.leadcontactevents.LeadContactEvent;
import ai.leadplus.domain.leadcontactevents.LeadContactEventSourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LeadContactEventDto {

    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long contactId;
    private LeadContactEventCategory category;
    private LeadContactEventType type;
    private String description;
    private Long sourceId;
    private LeadContactEventSourceType sourceType;
    private String eventBy;
    private LocalDateTime eventAt;
    private boolean active;
    private LocalDateTime createdAt;

    public static LeadContactEventDto fromEntity(LeadContactEvent event) {
        if (event == null) return null;
        return baseBuilder(LeadContactEventDto.builder(), event).build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends LeadContactEventDtoBuilder<?, ?>> T baseBuilder(T builder, LeadContactEvent event) {
        if (event == null) return null;
        return (T) builder
                .id(event.getId())
                .tenantId(event.getTenantId())
                .workspaceId(event.getWorkspaceId())
                .contactId(event.getContactId())
                .category(event.getCategory())
                .type(event.getType())
                .description(event.getDescription())
                .sourceId(event.getSourceId())
                .sourceType(event.getSourceType())
                .eventBy(event.getEventBy())
                .eventAt(event.getEventAt())
                .active(event.isActive())
                .createdAt(event.getCreatedAt());
    }

    public LeadContactEvent toEntity() {
        return LeadContactEvent.builder()
                .id(id)
                .tenantId(tenantId)
                .workspaceId(workspaceId)
                .contactId(contactId)
                .category(category)
                .type(type)
                .description(description)
                .sourceId(sourceId)
                .sourceType(sourceType)
                .eventBy(eventBy)
                .eventAt(eventAt)
                .active(active)
                .createdAt(createdAt)
                .build();
    }
}
