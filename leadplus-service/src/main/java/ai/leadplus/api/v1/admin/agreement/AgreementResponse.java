package ai.leadplus.api.v1.admin.agreement;

import ai.leadplus.api.v1.common.UserIdentity;
import ai.leadplus.application.agreement.AgreementDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.domain.agreement.AgreementType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AgreementResponse {

    private Long id;
    private int version;
    private String content;
    private AgreementType agreementType;
    private boolean latest;
    private LocalDateTime createdAt;
    private UserIdentity createdBy;

    public static AgreementResponse fromDto(AgreementDto agreement) {
        return AgreementResponse.builder()
                .id(agreement.getId())
                .version(agreement.getVersion())
                .content(agreement.getContent())
                .agreementType(agreement.getAgreementType())
                .latest(agreement.isLatest())
                .createdAt(agreement.getCreatedAt())
                .build();
    }

    public static AgreementResponse fromDto(AgreementDto agreement, UserDto userDto) {
        return AgreementResponse.builder()
                .id(agreement.getId())
                .version(agreement.getVersion())
                .content(agreement.getContent())
                .agreementType(agreement.getAgreementType())
                .latest(agreement.isLatest())
                .createdAt(agreement.getCreatedAt())
                .createdBy(userDto != null ? UserIdentity.fromEntity(userDto) : null)
                .build();
    }
}
