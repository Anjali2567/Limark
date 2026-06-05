package ai.leadplus.domain.requestforquotes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestForQuoteRepository extends JpaRepository<RequestForQuote, Long> {

    Optional<RequestForQuote> findByIdAndActiveTrue(Long id);

    Page<RequestForQuote> findAllByUserIdAndActiveTrue(Long userId, Pageable pageable);

    Page<RequestForQuote> findAllByVendorIdsContainingAndActiveTrue(
            Long vendorId,
            Pageable pageable
    );
}
