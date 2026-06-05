package ai.leadplus.domain.campaignemails;

import ai.leadplus.application.campaignemails.CampaignEmailCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CampaignEmailRepository extends JpaRepository<CampaignEmail, Long> {

    List<CampaignEmail> findAllByCampaignId(Long campaignId);

    Optional<CampaignEmail> findTopByCampaignIdOrderByStepNumberDesc(Long campaignId);

    Optional<CampaignEmail> findByCampaignIdAndStepNumber(Long campaignId, int stepNumber);

    boolean existsByCampaignId(Long campaignId);

    @Query("SELECT new ai.leadplus.application.campaignemails.CampaignEmailCountDto(e.campaignId, CAST(COUNT(e) AS int)) FROM CampaignEmail e WHERE e.campaignId IN :campaignIds GROUP BY e.campaignId")
    List<CampaignEmailCountDto> countCampaignEmailByCampaignIds(@Param("campaignIds") List<Long> campaignIds);

    @Modifying
    @Transactional
    @Query("UPDATE CampaignEmail e SET e.status = :newStatus WHERE e.campaignId = :campaignId AND e.status = :currentStatus")
    long findByCampaignIdAndStatus(
        @Param("campaignId") Long campaignId,
        @Param("currentStatus") CampaignEmailStatus status,
        @Param("newStatus") CampaignEmailStatus newStatus
    );

    long countByCampaignId(Long campaignId);

    Optional<CampaignEmail> findByIdAndCampaignId(Long emailId, Long campaignId);

    List<CampaignEmail> findAllByCampaignIdAndStepNumberGreaterThan(Long campaignId, int deletedStepNumber);
}
