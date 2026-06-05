package ai.leadplus.api.v1.tenants;

import ai.leadplus.api.v1.workspaces.RecipientRequest;
import ai.leadplus.application.tenants.TenantRecipientsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRecipientsRequest {
    private List<RecipientRequest> ccRecipients;
    private List<RecipientRequest> bccRecipients;

    public TenantRecipientsDto toDto() {
        return TenantRecipientsDto.builder()
                .ccRecipients(
                        CollectionUtils.isEmpty(ccRecipients)
                                ? List.of()
                                : ccRecipients.stream()
                                .map(RecipientRequest::toDto)
                                .toList()
                )
                .bccRecipients(
                        CollectionUtils.isEmpty(bccRecipients)
                                ? List.of()
                                : bccRecipients.stream()
                                .map(RecipientRequest::toDto)
                                .toList()
                )
                .build();
    }
}
