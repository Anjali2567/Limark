package ai.leadplus.api.v1.vendoragreement;

import ai.leadplus.application.vendoragreement.VendorAgreementDto;
import ai.leadplus.domain.agreement.AgreementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorAgreementRequest {
    private Long vendorId;
    private AgreementType agreementType;
    private String signedBy;
    private String name;
    private String title;
    private String otp;

    public VendorAgreementDto toDto() {
        return VendorAgreementDto.builder()
                .vendorId(this.vendorId)
                .agreementType(this.agreementType)
                .signedBy(this.signedBy)
                .name(this.name)
                .title(this.title)
                .build();
    }
}
