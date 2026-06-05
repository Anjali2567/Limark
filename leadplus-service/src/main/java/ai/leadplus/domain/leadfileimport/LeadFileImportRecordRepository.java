package ai.leadplus.domain.leadfileimport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadFileImportRecordRepository extends JpaRepository<LeadFileImportRecord, Long> {

    Page<LeadFileImportRecord> findAllByImportId(Long importId, Pageable pageable);

    List<LeadFileImportRecord> findAllByImportId(Long importId);
}