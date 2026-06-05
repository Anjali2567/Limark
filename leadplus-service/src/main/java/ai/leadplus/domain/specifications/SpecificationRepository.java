package ai.leadplus.domain.specifications;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpecificationRepository extends JpaRepository<Specification, Long> {

    Optional<Specification> findByIdAndActiveTrue(Long id);

    boolean existsByIdAndDisabledFalseAndActiveTrue(Long id);

    List<Specification> findAllByDisabledFalseAndActiveTrue();

    List<Specification> findAllByNameInIgnoreCaseAndDisabledFalseAndActiveTrue(List<String> names);

    List<Specification> findAllBySpecificationCategoryIdAndDisabledFalseAndActiveTrue(Long specificationCategoryId);

    List<Specification> findAllByIdInAndDisabledFalseAndActiveTrue(Collection<Long> ids);

    long countAllByIdInAndDisabledFalseAndActiveTrue(Collection<Long> ids);
}
