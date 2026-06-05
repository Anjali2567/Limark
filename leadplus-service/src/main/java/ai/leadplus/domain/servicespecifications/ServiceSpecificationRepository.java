package ai.leadplus.domain.servicespecifications;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceSpecificationRepository extends JpaRepository<ServiceSpecification, Long> {

    List<ServiceSpecification> findAllByServiceId(Long serviceId);

    boolean existsByServiceIdAndSpecificationId(Long serviceId, Long specificationId);

    void deleteByServiceIdAndSpecificationId(Long serviceId, Long specificationId);
}
