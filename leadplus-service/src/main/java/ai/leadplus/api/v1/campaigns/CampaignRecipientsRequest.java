package ai.leadplus.api.v1.campaigns;

import ai.leadplus.api.v1.common.RecipientRequest;
import ai.leadplus.application.campaigns.CampaignDto;
import jakarta.validation.Valid;
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
public class CampaignRecipientsRequest {

    @Valid
    private List<RecipientRequest> ccRecipients;
    @Valid
    private List<RecipientRequest> bccRecipients;

    public CampaignDto toDto(){
        return CampaignDto.builder()
                .ccRecipients(CollectionUtils.isEmpty(ccRecipients) ? List.of() : ccRecipients.stream().map(RecipientRequest::toDto).toList())
                .bccRecipients(CollectionUtils.isEmpty(bccRecipients) ? List.of() : bccRecipients.stream().map(RecipientRequest::toDto).toList())
                .build();
    }
}
