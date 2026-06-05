package ai.leadplus.domain.contactoutreachstatuses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContactOutreachStatusRepository extends JpaRepository<ContactOutreachStatus, Long> {

    List<ContactOutreachStatus> findAllByTenantIdAndContactIdIn(Long tenantId, List<Long> contactIds);

    @Query(value = """
            SELECT contact_id
            FROM contact_outreach_status
            WHERE tenant_id = :tenantId
              AND (
                status IN ('PAUSED', 'BOUNCED', 'UNSUBSCRIBED')
                OR (current_campaign_ids IS NOT NULL AND cardinality(current_campaign_ids) > 0)
                OR (status = 'ACTIVE' AND last_email_at IS NOT NULL AND last_email_at > :emailCutoff)
                OR (status = 'COMPLETED' AND sequence_completed_at IS NOT NULL AND sequence_completed_at > :cooldownCutoff)
              )
            """, nativeQuery = true)
    List<Long> findIneligibleContactIds(
            @Param("tenantId") Long tenantId,
            @Param("emailCutoff") LocalDateTime emailCutoff,
            @Param("cooldownCutoff") LocalDateTime cooldownCutoff
    );

    @Query(value = """
            SELECT contact_id
            FROM contact_outreach_status
            WHERE tenant_id = :tenantId
              AND contact_id IN (:contactIds)
              AND (
                status IN ('PAUSED', 'BOUNCED', 'UNSUBSCRIBED')
                OR (current_campaign_ids IS NOT NULL AND cardinality(current_campaign_ids) > 0)
                OR (status = 'ACTIVE' AND last_email_at IS NOT NULL AND last_email_at > :emailCutoff)
                OR (status = 'COMPLETED' AND sequence_completed_at IS NOT NULL AND sequence_completed_at > :cooldownCutoff)
              )
            """, nativeQuery = true)
    List<Long> findIneligibleContactIdsByTenantIdAndContactIdIn(
            @Param("tenantId") Long tenantId,
            @Param("contactIds") List<Long> contactIds,
            @Param("emailCutoff") LocalDateTime emailCutoff,
            @Param("cooldownCutoff") LocalDateTime cooldownCutoff
    );

    Optional<ContactOutreachStatus> findByTenantIdAndContactId(Long tenantId, Long contactId);

    Optional<ContactOutreachStatus> findByUnsubscribeToken(String unsubscribeToken);

    @Query(value = """
            SELECT *
            FROM contact_outreach_status
            WHERE tenant_id = :tenantId
              AND current_campaign_ids @> ARRAY[CAST(:campaignId AS varchar)]::varchar[]
            """, nativeQuery = true)
    List<ContactOutreachStatus> findAllByTenantIdAndCurrentCampaignIdsContaining(
            @Param("tenantId") Long tenantId,
            @Param("campaignId") Long campaignId
    );

    Page<ContactOutreachStatus> findAllByTenantIdAndStatus(Long tenantId, GlobalOutreachStatus status, Pageable pageable);

    Optional<ContactOutreachStatus> findTopByTenantIdAndLastEmailAtNotNullOrderByLastEmailAtDesc(Long tenantId);
}
