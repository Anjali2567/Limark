package ai.leadplus.application.agreement;

import ai.leadplus.domain.agreement.Agreement;
import ai.leadplus.domain.agreement.AgreementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgreementDto {

    private Long id;
    private int version;
    private String content;
    private AgreementType agreementType;
    private boolean latest;
    private LocalDateTime createdAt;
    private Long createdBy;

    public static AgreementDto toDto(Agreement agreement) {
        return AgreementDto.builder()
                .id(agreement.getId())
                .version(agreement.getVersion())
                .content(agreement.getContent())
                .agreementType(agreement.getAgreementType())
                .latest(agreement.isLatest())
                .createdAt(agreement.getCreatedAt())
                .createdBy(agreement.getCreatedBy())
                .build();
    }

    public Agreement toEntity() {
        return Agreement.builder()
                .id(id)
                .version(version)
                .content(content)
                .agreementType(agreementType)
                .latest(latest)
                .createdAt(createdAt)
                .build();
    }
}
