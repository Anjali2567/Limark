package ai.leadplus.application.requestforquotes;

import ai.leadplus.application.attachments.AttachmentDto;
import ai.leadplus.application.services.ServiceDto;
import ai.leadplus.domain.requestforquotes.RequestForQuote;
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
public class RequestForQuoteDetailedDto extends RequestForQuoteDto{

    private List<ServiceDto> services;
    private List<AttachmentDto> attachments;

    public static RequestForQuoteDetailedDto fromEntity(RequestForQuote requestForQuote, List<AttachmentDto> attachments) {
        return baseBuilder(RequestForQuoteDetailedDto.builder(), requestForQuote)
                .attachments(attachments)
                .build();
    }
}
