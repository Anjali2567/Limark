package ai.leadplus.domain.leadcompanies;

import ai.leadplus.domain.leads.LeadFacetValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadCompanyRepository extends JpaRepository<LeadCompany, Long>, JpaSpecificationExecutor<LeadCompany> {

    Optional<LeadCompany> findByDomainAndActiveTrue(String domain);

    Optional<LeadCompany> findByIdAndActiveTrue(Long id);


    Page<LeadCompany> findAllByUpdatedAtAfterAndActiveTrue(
            LocalDateTime updatedAt,
            Pageable pageable
    );

    @Query(value = "SELECT DISTINCT unnest(segments) FROM lead_company WHERE active = true AND segments IS NOT NULL", nativeQuery = true)
    List<String> findDistinctSegments();

    @Query(value = """
            SELECT DISTINCT trim(industry)
            FROM lead_company
            WHERE active = true
              AND industry IS NOT NULL
              AND trim(industry) <> ''
            ORDER BY trim(industry)
            """, nativeQuery = true)
    List<String> findDistinctIndustries();

    @Query(value = "SELECT id FROM lead_company WHERE active = true", nativeQuery = true)
    List<Long> findAllActiveIds();

    @Query(value = """
            SELECT trim(lc.industry) AS value, COUNT(*) AS count
            FROM lead_company lc
            WHERE lc.active = true
              AND lc.industry IS NOT NULL
              AND trim(lc.industry) <> ''
              AND (lc.tenant_ids IS NULL OR lc.tenant_ids @> ARRAY[CAST(:tenantId AS varchar)]::varchar[])
            GROUP BY trim(lc.industry)
            ORDER BY COUNT(*) DESC, trim(lc.industry) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<LeadFacetValue> findTopIndustryValuesByTenantId(@Param("tenantId") Long tenantId, @Param("limit") int limit);

    @Query(value = """
            SELECT trim(x) AS value, COUNT(*) AS count FROM lead_company lc
            CROSS JOIN LATERAL unnest(lc.naics_codes) x
            WHERE lc.active = true
              AND lc.segments @> ARRAY['Automate26']::varchar[]
              AND (lc.tenant_ids IS NULL OR lc.tenant_ids @> ARRAY[CAST(:tenantId AS varchar)]::varchar[])
              AND x IS NOT NULL
              AND trim(x) <> ''
            GROUP BY trim(x)
            ORDER BY COUNT(*) DESC, trim(x) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<LeadFacetValue> findTopNaicsValuesByTenantId(@Param("tenantId") Long tenantId, @Param("limit") int limit);

    @Query(value = """
            SELECT trim(x) AS value, COUNT(*) AS count FROM lead_company lc
            CROSS JOIN LATERAL unnest(lc.sic_codes) x
            WHERE lc.active = true
              AND lc.segments @> ARRAY['Automate26']::varchar[]
              AND (lc.tenant_ids IS NULL OR lc.tenant_ids @> ARRAY[CAST(:tenantId AS varchar)]::varchar[])
              AND x IS NOT NULL
              AND trim(x) <> ''
            GROUP BY trim(x)
            ORDER BY COUNT(*) DESC, trim(x) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<LeadFacetValue> findTopSicValuesByTenantId(@Param("tenantId") Long tenantId, @Param("limit") int limit);

    @Query(value = """
            SELECT lc.id FROM lead_company lc
            WHERE lc.active = true
            AND lc.segments && CAST(:segments AS varchar[])
            AND (lc.tenant_ids IS NULL OR lc.tenant_ids @> CAST(:tenantId AS varchar[]))
            """, nativeQuery = true)
    List<Long> findGatedCompanyIds(
            @Param("segments") String segments,
            @Param("tenantId") String tenantId
    );

    @Query(value = """
            SELECT lc.id FROM lead_company lc
            WHERE lc.active = true
            AND (
                lc.segments && CAST(:segments AS varchar[])
                OR lc.segments IS NULL
                OR cardinality(lc.segments) = 0
            )
            AND (lc.tenant_ids IS NULL OR lc.tenant_ids @> CAST(:tenantId AS varchar[]))
            """, nativeQuery = true)
    List<Long> findGatedCompanyIdsWithNullAccess(
            @Param("segments") String segments,
            @Param("tenantId") String tenantId
    );

    List<LeadCompany> findAllByIdInAndActiveTrue(List<Long> ids);

    long countAllByIdInAndActiveTrue(List<Long> ids);

    boolean existsByIdAndActiveTrue(Long contactId);

    @Query("SELECT l FROM LeadCompany l WHERE l.active = true AND l.websiteUrl IS NOT NULL AND l.id NOT IN ?1")
    List<LeadCompany> findActiveCompaniesWithWebsiteUrlExcluding(List<Long> excludedIds, Pageable pageable);

    @Modifying
    @Query("UPDATE LeadCompany l SET l.scrapedTechnologies = ?2, l.scrapedTools = ?3, l.scrapedServices = ?4 WHERE l.id = ?1")
    void updateScrapedTech(Long id, List<String> technologies, List<String> tools, List<String> services);
}
