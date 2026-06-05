package ai.leadplus.domain.agreement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, Long> {

    Page<Agreement> findAllByAgreementType(AgreementType agreementType, Pageable pageable);

    Agreement findAgreementByAgreementTypeAndLatestIsTrue(AgreementType agreementType);

}
