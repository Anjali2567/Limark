package ai.leadplus.domain.leadcontacts;

import ai.leadplus.domain.leads.LeadFacetValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadContactRepository extends JpaRepository<LeadContact, Long>, JpaSpecificationExecutor<LeadContact> {

    // INDEX REQUIRED: { active: 1, leadCompanyId: 1 } on lead_contacts
    Optional<LeadContact> findByIdAndLeadCompanyIdAndActiveTrue(Long id, Long companyId);

    // INDEX REQUIRED: { _id: 1, active: 1 } on lead_contacts
    Optional<LeadContact> findByIdAndActiveTrue(Long id);

    // INDEX REQUIRED: { active: 1, leadCompanyId: 1 } on lead_contacts
    Optional<LeadContact> findByLeadCompanyIdAndFullNameIgnoreCaseAndActiveTrue(Long companyId, String fullName);

    // INDEX REQUIRED: { active: 1, leadCompanyId: 1 } on lead_contacts
    boolean existsByLeadCompanyIdAndFullNameIgnoreCaseAndActiveTrue(Long companyId, String fullName);

    // INDEX REQUIRED: { active: 1, leadCompanyId: 1 } on lead_contacts
    // Aggregation query for grouping contacts by company
    @Query(value = "SELECT new ai.leadplus.domain.leadcontacts.CompanyContactCount(l.leadCompanyId, COUNT(l)) " +
            "FROM LeadContact l WHERE l.active = true AND l.leadCompanyId IN ?1 " +
            "AND l.email IS NOT NULL AND l.email != '' " +
            "GROUP BY l.leadCompanyId")
    List<CompanyContactCount> countContactsByCompanyIds(List<Long> companyIds);

    // INDEX REQUIRED: { _id: 1, active: 1 } on lead_contacts
    List<LeadContact> findAllByIdInAndActiveTrue(List<Long> ids);

    // INDEX REQUIRED: { active: 1, leadCompanyId: 1 } on lead_contacts
    List<LeadContact> findAllByLeadCompanyIdInAndActiveTrue(List<Long> companyIds);

    // INDEX REQUIRED: { active: 1, updatedAt: -1 } on lead_contacts
    Page<LeadContact> findAllByUpdatedAtAfterAndActiveTrue(LocalDateTime updatedAt, Pageable pageable);

    // INDEX REQUIRED: { _id: 1, active: 1 } on lead_contacts
    long countAllByIdInAndActiveTrue(List<Long> ids);

    // INDEX REQUIRED: { _id: 1, active: 1 } on lead_contacts
    boolean existsByIdAndActiveTrue(Long id);

    // INDEX REQUIRED: { _id: 1, active: 1 } on lead_contacts
    Page<LeadContact> findAllByIdInAndActiveTrue(List<Long> ids, Pageable pageable);

    // INDEX REQUIRED: { leadCompanyId: 1, active: 1, email: 1 } on lead_contacts
    @Query("SELECT l FROM LeadContact l WHERE l.leadCompanyId = ?1 AND l.email IS NOT NULL AND l.email != '' AND l.active = true")
    Page<LeadContact> findValidContacts(Long companyId, Pageable pageable);

    Optional<LeadContact> findByEmailAndActiveTrue(String email);

    @Query(value = """
            SELECT trim(lc.title) AS value, COUNT(*) AS count FROM lead_contact lc
            JOIN lead_company lco ON lco.id = lc.lead_company_id
            WHERE lc.active = true
              AND lco.active = true
              AND lco.segments @> ARRAY['Automate26']::varchar[]
              AND (lco.tenant_ids IS NULL OR lco.tenant_ids @> ARRAY[CAST(:tenantId AS varchar)]::varchar[])
              AND (lc.tenant_ids IS NULL OR lc.tenant_ids @> ARRAY[CAST(:tenantId AS varchar)]::varchar[])
              AND lc.title IS NOT NULL
              AND trim(lc.title) <> ''
            GROUP BY trim(lc.title)
            ORDER BY COUNT(*) DESC, trim(lc.title) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<LeadFacetValue> findTopTitleValuesByTenantId(@Param("tenantId") Long tenantId, @Param("limit") int limit);

    @Query(value = """
            SELECT trim(lc.department) AS value, COUNT(*) AS count FROM lead_contact lc
            JOIN lead_company lco ON lco.id = lc.lead_company_id
            WHERE lc.active = true
              AND lco.active = true
              AND lco.segments @> ARRAY['Automate26']::varchar[]
              AND (lco.tenant_ids IS NULL OR lco.tenant_ids @> ARRAY[CAST(:tenantId AS varchar)]::varchar[])
              AND (lc.tenant_ids IS NULL OR lc.tenant_ids @> ARRAY[CAST(:tenantId AS varchar)]::varchar[])
              AND lc.department IS NOT NULL
              AND trim(lc.department) <> ''
            GROUP BY trim(lc.department)
            ORDER BY COUNT(*) DESC, trim(lc.department) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<LeadFacetValue> findTopDepartmentValuesByTenantId(@Param("tenantId") Long tenantId, @Param("limit") int limit);
}