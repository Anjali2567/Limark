package ai.leadplus.application.vendorshowcases;

import ai.leadplus.application.attachments.AttachmentDto;
import ai.leadplus.domain.vendorshowcases.VendorShowcase;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorShowcaseWithAttachmentsDto extends  VendorShowcaseDto {

    private List<AttachmentDto> attachments;

    public static VendorShowcaseWithAttachmentsDto fromEntity(VendorShowcase vendorShowcase, List<AttachmentDto> attachments) {
        return baseBuilder(VendorShowcaseWithAttachmentsDto.builder(), vendorShowcase)
                .attachments(attachments)
                .build();
    }
}
