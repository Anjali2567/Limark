package ai.leadplus.api.v1.vendoragreement;

import ai.leadplus.application.vendoragreement.VendorAgreementDto;
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
public class VendorAgreementResponse {
    private Long vendorId;
    private AgreementType agreementType;
    private int version;
    private String agreementText;
    private boolean signed;
    private LocalDateTime signedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VendorAgreementResponse fromDto(VendorAgreementDto dto) {
        return VendorAgreementResponse.builder()
                .vendorId(dto.getVendorId())
                .agreementType(dto.getAgreementType())
                .version(dto.getVersion())
                .agreementText(dto.getAgreementText())
                .signed(dto.isSigned())
                .signedAt(dto.getSignedAt())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
