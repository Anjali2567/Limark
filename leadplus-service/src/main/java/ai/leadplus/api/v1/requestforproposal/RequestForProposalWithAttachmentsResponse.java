package ai.leadplus.api.v1.requestforproposal;

import ai.leadplus.api.v1.common.AttachmentResponse;
import ai.leadplus.application.requestforproposal.RequestForProposalWithAttachmentsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForProposalWithAttachmentsResponse extends RequestForProposalResponse {
    private List<AttachmentResponse> attachments;

    public static RequestForProposalWithAttachmentsResponse fromDto(RequestForProposalWithAttachmentsDto dto) {
        List<AttachmentResponse> attachmentResponses = CollectionUtils.isEmpty(dto.getAttachments()) ?
                List.of() :
                dto.getAttachments().stream()
                        .map(AttachmentResponse::fromDto)
                        .toList();
        return baseBuilder(RequestForProposalWithAttachmentsResponse.builder(), dto)
                .attachments(attachmentResponses)
                .build();
    }
}
