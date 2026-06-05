package ai.leadplus.domain.vendors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long>, JpaSpecificationExecutor<Vendor> {

    Optional<Vendor> findByUserIdAndActiveTrue(Long userId);

    Optional<Vendor> findByIdAndActiveTrue(Long id);

    long countByIdInAndActiveTrue(Collection<Long> ids);

    long countAllByIdInAndActiveTrue(Collection<Long> ids);

    List<Vendor> findAllByIdInAndActiveTrue(Collection<Long> ids);
}
