package ai.leadplus.api.v1.requestforquotes;

import ai.leadplus.api.v1.common.AttachmentResponse;
import ai.leadplus.api.v1.services.ServiceResponse;
import ai.leadplus.application.requestforquotes.RequestForQuoteDetailedDto;
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
public class RequestForQuoteDetailedResponse extends RequestForQuoteResponse {

    private List<ServiceResponse> services;
    private List<AttachmentResponse> attachments;
    private int numberOfQuotationsForRfq;
    private int numberOfVendorsForRfq;

    public static RequestForQuoteDetailedResponse fromDto(RequestForQuoteDetailedDto dto) {
        return fromDto(dto, 0, 0);
    }

    public static RequestForQuoteDetailedResponse fromDto(RequestForQuoteDetailedDto dto,
                                                          int numberOfQuotationsForRfq,
                                                          int numberOfVendorsForRfq) {
        List<ServiceResponse> serviceResponses = mapServices(dto);
        List<AttachmentResponse> attachmentResponses = mapAttachments(dto);

        return baseBuilder(RequestForQuoteDetailedResponse.builder(), dto)
                .services(serviceResponses)
                .attachments(attachmentResponses)
                .numberOfQuotationsForRfq(numberOfQuotationsForRfq)
                .numberOfVendorsForRfq(numberOfVendorsForRfq)
                .build();
    }

    private static List<ServiceResponse> mapServices(RequestForQuoteDetailedDto dto) {
        return CollectionUtils.isEmpty(dto.getServices()) ?
                List.of() :
                dto.getServices().stream()
                        .map(ServiceResponse::fromDto)
                        .toList();
    }

    private static List<AttachmentResponse> mapAttachments(RequestForQuoteDetailedDto dto) {
        return CollectionUtils.isEmpty(dto.getAttachments()) ?
                List.of() :
                dto.getAttachments().stream()
                        .map(AttachmentResponse::fromDto)
                        .toList();
    }
}