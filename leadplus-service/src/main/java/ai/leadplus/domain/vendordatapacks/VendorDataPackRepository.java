package ai.leadplus.domain.vendordatapacks;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorDataPackRepository extends JpaRepository<VendorDataPack, Long> {

    Optional<VendorDataPack> findByIdAndActiveTrue(Long id);

    Optional<VendorDataPack> findByIdAndTenantIdAndVendorIdAndActiveTrue(Long id, Long tenantId, Long vendorId);

    List<VendorDataPack> findAllByTenantIdAndVendorIdAndActiveTrue(
            Long tenantId, Long vendorId);
}
