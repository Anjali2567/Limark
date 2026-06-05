package ai.leadplus.domain.leadfileimport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadFileImportRepository extends JpaRepository<LeadFileImport, Long> {

    Page<LeadFileImport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<LeadFileImport> findAllByTargetEntityOrderByCreatedAtDesc(TargetEntity targetEntity, Pageable pageable);
}