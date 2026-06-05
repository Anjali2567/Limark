package ai.leadplus.domain.vendoragreement;

import ai.leadplus.domain.agreement.AgreementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorAgreementRepository extends JpaRepository<VendorAgreement, Long> {

    VendorAgreement findByVendorIdAndAgreementType(Long vendorId, AgreementType agreementType);
}
