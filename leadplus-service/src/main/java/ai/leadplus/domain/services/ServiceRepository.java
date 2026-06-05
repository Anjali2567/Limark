package ai.leadplus.domain.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findByIdAndActiveTrue(Long id);

    Optional<Service> findBySlugAndActiveTrue(String slug);

    Page<Service> findAllByActiveTrue(Pageable pageable);

    List<Service> findAllByServiceCategoryIdAndDisabledFalseAndActiveTrue(Long serviceCategoryId);

    List<Service> findAllByIdInAndDisabledFalseAndActiveTrue(Collection<Long> ids);

    long countAllByIdInAndDisabledFalseAndActiveTrue(Collection<Long> ids);

    List<Service> findAllByDisabledFalseAndActiveTrue();

    List<Service> findAllByNameInIgnoreCaseAndDisabledFalseAndActiveTrue(Collection<String> names);

    boolean existsByIdAndActiveTrueAndDisabledFalse(Long serviceId);
}
