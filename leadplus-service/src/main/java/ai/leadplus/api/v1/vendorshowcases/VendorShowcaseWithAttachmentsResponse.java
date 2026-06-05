package ai.leadplus.api.v1.vendorshowcases;

import ai.leadplus.api.v1.common.AttachmentResponse;
import ai.leadplus.application.vendorshowcases.VendorShowcaseWithAttachmentsDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorShowcaseWithAttachmentsResponse extends VendorShowcaseResponse{

    private List<AttachmentResponse> attachments;

    public static VendorShowcaseWithAttachmentsResponse fromDto(VendorShowcaseWithAttachmentsDto dto) {
        List<AttachmentResponse> attachmentResponses = CollectionUtils.isEmpty(dto.getAttachments()) ?
                List.of() :
                dto.getAttachments().stream()
                        .map(AttachmentResponse::fromDto)
                        .toList();
        return baseBuilder(VendorShowcaseWithAttachmentsResponse.builder(), dto)
                .attachments(attachmentResponses)
                .build();
    }
}
