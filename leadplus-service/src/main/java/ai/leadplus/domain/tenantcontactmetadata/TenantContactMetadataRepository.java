package ai.leadplus.domain.tenantcontactmetadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantContactMetadataRepository extends JpaRepository<TenantContactMetadata, Long> {

    Optional<TenantContactMetadata> findByTenantIdAndLeadContactId(Long tenantId, Long leadContactId);

    List<TenantContactMetadata> findAllByTenantIdAndLeadContactIdIn(Long tenantId, List<Long> leadContactIds);

    @Query("SELECT DISTINCT t.bdName FROM TenantContactMetadata t WHERE t.tenantId = :tenantId AND t.bdName IS NOT NULL AND t.bdName <> ''")
    List<String> findDistinctBdNames(@Param("tenantId") Long tenantId);

    @Query("SELECT DISTINCT t.isrName FROM TenantContactMetadata t WHERE t.tenantId = :tenantId AND t.isrName IS NOT NULL AND t.isrName <> ''")
    List<String> findDistinctIsrNames(@Param("tenantId") Long tenantId);

    @Query("SELECT DISTINCT t.priority FROM TenantContactMetadata t WHERE t.tenantId = :tenantId AND t.priority IS NOT NULL AND t.priority <> ''")
    List<String> findDistinctPriorities(@Param("tenantId") Long tenantId);

    @Query("SELECT DISTINCT t.titleCategory FROM TenantContactMetadata t WHERE t.tenantId = :tenantId AND t.titleCategory IS NOT NULL AND t.titleCategory <> ''")
    List<String> findDistinctTitleCategories(@Param("tenantId") Long tenantId);
}
