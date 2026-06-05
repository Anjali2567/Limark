package ai.leadplus.domain.industries;

import ai.leadplus.application.campaignagent.tools.IndustryAccessDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IndustryRepository extends JpaRepository<Industry, Long> {

    Optional<Industry> findByIdAndActiveTrue(Long id);

    Optional<Industry> findBySlugAndActiveTrue(String slug);

    boolean existsByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    Page<Industry> findAllByDisabledFalseAndActiveTrue(Pageable pageable);

    Page<Industry> findAllByNameContainingIgnoreCaseAndDisabledFalseAndActiveTrue(String query, Pageable pageable);

    Page<Industry> findAllByActiveTrue(Pageable pageable);

    Page<Industry> findAllByNameContainingIgnoreCaseAndActiveTrue(String query, Pageable pageable);

    List<Industry> findAllByDisabledFalseAndActiveTrue();

    List<Industry> findAllByNameInIgnoreCaseAndDisabledFalseAndActiveTrue(List<String> names);

    long countByIdInAndActiveTrue(Collection<Long> ids);

    long countByIdInAndDisabledFalseAndActiveTrue(Collection<Long> ids);

    boolean existsByIdAndDisabledFalseAndActiveTrue(Long id);

    List<Industry> findAllByIdInAndActiveTrue(Collection<Long> ids);

    @Query("SELECT new ai.leadplus.application.campaignagent.tools.IndustryAccessDto(i.name, i.segments) FROM Industry i WHERE i.active = true AND i.disabled = false")
    List<IndustryAccessDto> findAllIndustryAccess();

    boolean existsBySlugIgnoreCaseAndActiveTrue(String slug);
}
