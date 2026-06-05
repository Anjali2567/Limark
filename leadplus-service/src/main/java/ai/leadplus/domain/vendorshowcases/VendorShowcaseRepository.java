package ai.leadplus.domain.vendorshowcases;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorShowcaseRepository extends JpaRepository<VendorShowcase, Long> {

    Optional<VendorShowcase> findByIdAndActiveTrue(Long id);

    List<VendorShowcase> findAllByVendorIdAndActiveTrue(Long vendorId);
}
