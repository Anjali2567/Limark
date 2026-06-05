package ai.leadplus.api.v1.tenantannouncements;

import ai.leadplus.application.tenantannouncements.TenantAnnouncementDto;
import ai.leadplus.application.common.RecipientDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAnnouncementRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;

    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private List<String> attachmentIds;

    public TenantAnnouncementDto toDto() {
        return TenantAnnouncementDto.builder()
                .name(name)
                .subject(subject)
                .body(body)
                .ccRecipients(ccRecipients)
                .bccRecipients(bccRecipients)
                .attachmentIds(attachmentIds)
                .build();
    }
}
