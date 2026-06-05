package ai.leadplus.application.contactoutreachstatuses;

import ai.leadplus.domain.contactoutreachstatuses.ContactOutreachStatus;
import ai.leadplus.domain.contactoutreachstatuses.GlobalOutreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactOutreachStatusDto {

    private Long id;
    private Long contactId;
    private Long tenantId;
    private List<Long> currentCampaignIds;
    private LocalDateTime lastEmailAt;
    private GlobalOutreachStatus status;
    private LocalDateTime sequenceCompletedAt;
    private String unsubscribeToken;
    private LocalDateTime updatedAt;

    public static ContactOutreachStatusDto fromEntity(ContactOutreachStatus contactOutreachStatus) {
        return ContactOutreachStatusDto.builder()
                .id(contactOutreachStatus.getId())
                .contactId(contactOutreachStatus.getContactId())
                .tenantId(contactOutreachStatus.getTenantId())
                .currentCampaignIds(contactOutreachStatus.getCurrentCampaignIds())
                .lastEmailAt(contactOutreachStatus.getLastEmailAt())
                .status(contactOutreachStatus.getStatus())
                .sequenceCompletedAt(contactOutreachStatus.getSequenceCompletedAt())
                .unsubscribeToken(contactOutreachStatus.getUnsubscribeToken())
                .updatedAt(contactOutreachStatus.getUpdatedAt())
                .build();
    }
}
