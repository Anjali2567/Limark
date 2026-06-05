package ai.leadplus.application.vendoragreement;

import ai.leadplus.domain.agreement.AgreementType;
import ai.leadplus.domain.vendoragreement.Otp;
import ai.leadplus.domain.vendoragreement.VendorAgreement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorAgreementDto {
    private Long id;
    private Long vendorId;
    private AgreementType agreementType;
    private int version;
    private String agreementText;
    private Otp otp;
    private boolean verified;
    private boolean signed;
    private String signedBy;
    private String name;
    private String title;
    private LocalDateTime signedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VendorAgreementDto fromEntity(VendorAgreement agreement) {
        return VendorAgreementDto.builder()
                .id(agreement.getId())
                .vendorId(agreement.getVendorId())
                .agreementType(agreement.getAgreementType())
                .version(agreement.getVersion())
                .agreementText(agreement.getAgreementText())
                .otp(agreement.getOtp())
                .verified(agreement.isVerified())
                .signed(agreement.isSigned())
                .signedBy(agreement.getSignedBy())
                .name(agreement.getName())
                .title(agreement.getTitle())
                .signedAt(agreement.getSignedAt())
                .createdAt(agreement.getCreatedAt())
                .updatedAt(agreement.getUpdatedAt())
                .build();
    }

    public VendorAgreement toEntity() {
        return VendorAgreement.builder()
                .id(id)
                .vendorId(vendorId)
                .agreementType(agreementType)
                .version(version)
                .agreementText(agreementText)
                .otp(otp)
                .verified(verified)
                .signed(signed)
                .signedBy(signedBy)
                .name(name)
                .title(title)
                .signedAt(signedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
