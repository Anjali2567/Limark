package ai.leadplus.domain.specificationcategories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpecificationCategoryRepository extends JpaRepository<SpecificationCategory, Long> {

    Optional<SpecificationCategory> findByIdAndActiveTrue(Long id);

    Page<SpecificationCategory> findAllByActiveTrue(Pageable pageable);

    List<SpecificationCategory> findAllByActiveTrue();

    boolean existsByIdAndActiveTrue(Long id);
}
