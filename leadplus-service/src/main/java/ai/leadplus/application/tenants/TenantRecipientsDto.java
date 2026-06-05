package ai.leadplus.application.tenants;

import ai.leadplus.application.common.RecipientDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRecipientsDto {
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
}
