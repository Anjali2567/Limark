package ai.leadplus.domain.quotations;

import ai.leadplus.domain.common.SourcingRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByIdAndActiveTrue(Long id);

    Optional<Quotation> findByIdAndSourceTypeAndActiveTrue(Long id, SourcingRequestType sourceType);

    Optional<Quotation> findByIdAndVendorIdAndSourceTypeAndActiveTrue(Long id, Long vendorId, SourcingRequestType sourceType);

    Page<Quotation> findAllByVendorIdAndSourceTypeAndActiveTrue(Long vendorId, SourcingRequestType sourceType, Pageable pageable);

    List<Quotation> findAllBySourceIdAndSourceTypeAndActiveTrue(
            Long sourceId,
            SourcingRequestType sourceType
    );

    Page<Quotation> findAllBySourceIdAndSourceTypeAndActiveTrue(Long sourceId, SourcingRequestType sourceType, Pageable pageable);

    int countBySourceIdAndSourceTypeAndStatusInAndActiveTrue(Long sourceId, SourcingRequestType sourceType, List<QuotationStatus> status);
}
