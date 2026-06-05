package ai.leadplus.application.requestforproposal;

import ai.leadplus.application.attachments.AttachmentDto;
import ai.leadplus.domain.requestforproposal.RequestForProposal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForProposalWithAttachmentsDto extends RequestForProposalDto {

    private List<AttachmentDto> attachments;

    public static RequestForProposalWithAttachmentsDto fromEntity(RequestForProposal requestForProposal, List<AttachmentDto> attachments) {
        return baseBuilder(RequestForProposalWithAttachmentsDto.builder(), requestForProposal)
                .attachments(attachments)
                .build();
    }
}
