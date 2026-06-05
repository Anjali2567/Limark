package ai.leadplus.domain.leaddatapacks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LeadDataPackRepository extends JpaRepository<LeadDataPack, Long> {

    Optional<LeadDataPack> findByIdAndActiveTrue(Long id);

    Optional<LeadDataPack> findBySlugAndActiveTrue(String slug);

    Page<LeadDataPack> findAllByActiveTrue(Pageable pageable);

    List<LeadDataPack> findAllByActiveTrue();

    List<LeadDataPack> findAllByIdInAndActiveTrue(Collection<Long> ids);
}