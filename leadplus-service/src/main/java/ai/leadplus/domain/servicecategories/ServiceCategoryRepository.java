package ai.leadplus.domain.servicecategories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    Optional<ServiceCategory> findByIdAndActiveTrue(Long id);

    List<ServiceCategory> findAllByActiveTrue();

    Page<ServiceCategory> findAllByActiveTrue(Pageable pageable);

    boolean existsByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);
}
