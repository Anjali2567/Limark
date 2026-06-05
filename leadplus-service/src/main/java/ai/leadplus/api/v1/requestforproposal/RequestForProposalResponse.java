package ai.leadplus.api.v1.requestforproposal;

import ai.leadplus.application.requestforproposal.RequestForProposalDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForProposalResponse {

    private Long id;
    private String title;
    private String timeline;
    private String description;

    public static RequestForProposalResponse fromDto(RequestForProposalDto dto) {
        if (dto == null) {
            return null;
        }
        return baseBuilder(RequestForProposalResponse.builder(), dto)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends RequestForProposalResponseBuilder<?,?>> T baseBuilder(T builder, RequestForProposalDto dto) {
        if (dto == null) {
            return null;
        }

        return (T) builder
                .id(dto.getId())
                .title(dto.getTitle())
                .timeline(dto.getTimeline())
                .description(dto.getDescription());
    }
}
