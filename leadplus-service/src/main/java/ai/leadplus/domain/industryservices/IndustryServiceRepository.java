package ai.leadplus.domain.industryservices;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndustryServiceRepository extends JpaRepository<IndustryServiceMapping, Long> {

    List<IndustryServiceMapping> findAllByIndustryId(Long industryId);

    List<IndustryServiceMapping> findAllByServiceId(Long serviceId);

    boolean existsByIndustryIdAndServiceId(Long industryId, Long serviceId);

    void deleteByIndustryIdAndServiceId(Long industryId, Long serviceId);
}
