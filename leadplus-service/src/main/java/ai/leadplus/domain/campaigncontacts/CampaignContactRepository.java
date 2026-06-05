package ai.leadplus.domain.campaigncontacts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CampaignContactRepository extends JpaRepository<CampaignContact, Long> {

    Optional<CampaignContact> findTopByStatusAndNextSendAtBeforeAndParticipatingTrueOrderByUpdatedAtAsc(
            CampaignContactStatus status,
            LocalDateTime nextSendAt
    );

    List<CampaignContact> findAllByCampaignId(Long campaignId);

    List<CampaignContact> findAllByCampaignIdAndParticipatingTrue(Long campaignId);

    List<CampaignContact> findAllByCampaignIdAndIdIn(Long campaignId, List<Long> ids);

    List<CampaignContact> findAllByCampaignIdIn(Collection<Long> campaignIds);

    List<CampaignContact> findAllByCampaignIdAndContactIdIn(Long campaignId, List<Long> ids);

    List<CampaignContact> findAllByCampaignIdInAndParticipatingTrue(Collection<Long> campaignIds);

    boolean existsByCampaignIdAndStatusAndCurrentStepAndParticipatingTrue(
            Long campaignId,
            CampaignContactStatus status,
            int currentStep
    );

    List<CampaignContact> findAllByCampaignIdAndStatusAndParticipatingTrue(Long campaignId, CampaignContactStatus status);

    @Query(value = """
            SELECT * FROM campaign_contact
            WHERE email_data IS NOT NULL
              AND last_sent_at >= :since
              AND EXISTS (
                SELECT 1 FROM jsonb_array_elements(email_data::jsonb) AS ed
                WHERE ed->>'emailPlatform' = :emailPlatform
                  AND ed->>'emailDeliveryStatus' = :emailDeliveryStatus
              )
            ORDER BY id
            """,
            countQuery = """
            SELECT COUNT(*) FROM campaign_contact
            WHERE email_data IS NOT NULL
              AND last_sent_at >= :since
              AND EXISTS (
                SELECT 1 FROM jsonb_array_elements(email_data::jsonb) AS ed
                WHERE ed->>'emailPlatform' = :emailPlatform
                  AND ed->>'emailDeliveryStatus' = :emailDeliveryStatus
              )
            """,
            nativeQuery = true)
    Page<CampaignContact> findAllByEmailDataEmailPlatformAndEmailDataEmailDeliveryStatus(
            @Param("emailPlatform") String emailPlatform,
            @Param("emailDeliveryStatus") String emailDeliveryStatus,
            @Param("since") LocalDateTime since,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("UPDATE CampaignContact c SET c.status = :newStatus WHERE c.campaignId = :campaignId AND c.status = :currentStatus AND c.participating = true")
    long findByCampaignIdAndStatusAndParticipatingTrue(
            @Param("campaignId") Long campaignId,
            @Param("currentStatus") CampaignContactStatus currentStatus,
            @Param("newStatus") CampaignContactStatus newStatus
    );

    List<CampaignContact> findAllByContactIdAndCampaignIdInAndStatus(
            Long contactId,
            Collection<Long> campaignIds,
            CampaignContactStatus status
    );
}
