package ai.leadplus.application.campaigns;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaigncontacts.CampaignContactService;
import ai.leadplus.application.campaigncontacts.EmailDataDto;
import ai.leadplus.application.campaigncontacts.EmailDeliveryStatus;
import ai.leadplus.application.campaignemails.CampaignEmailService;
import ai.leadplus.domain.campaigncontacts.CampaignContactStatus;
import ai.leadplus.domain.campaigns.Campaign;
import ai.leadplus.domain.campaigns.CampaignRepository;
import ai.leadplus.domain.campaigns.CampaignStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignSearchService {

    private final CampaignRepository campaignRepository;
    private final CampaignContactService campaignContactService;
    private final CampaignEmailService campaignEmailService;

    public Page<CampaignListDto> searchCampaigns(
            Long tenantId, Long workspaceId,
            String query, List<CampaignStatus> campaignStatuses, List<String> industries,
            Pageable pageable) {

        Specification<Campaign> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("workspaceId"), workspaceId));

            if (StringUtils.isNotBlank(query)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.trim().toLowerCase() + "%"));
            }

            if (!CollectionUtils.isEmpty(campaignStatuses)) {
                predicates.add(root.get("status").in(campaignStatuses));
            }

            if (!CollectionUtils.isEmpty(industries)) {
                predicates.add(root.get("industry").in(industries));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Campaign> campaignsPage = campaignRepository.findAll(spec, pageable);
        List<Campaign> campaigns = campaignsPage.getContent();

        if (campaigns.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, campaignsPage.getTotalElements());
        }

        List<Long> campaignIds = campaigns.stream().map(Campaign::getId).toList();

        List<CampaignContactInfoDto> allContacts = campaignContactService
                .getCampaignContactsByCampaignIds(tenantId, campaignIds, true);
        Map<Long, List<CampaignContactInfoDto>> contactsByCampaignId = allContacts.stream()
                .collect(Collectors.groupingBy(CampaignContactInfoDto::getCampaignId));

        Map<Long, Integer> emailStepsByCampaignId = campaignEmailService
                .getCampaignEmailCountByCampaignIds(campaignIds);

        List<CampaignListDto> content = campaigns.stream()
                .map(c -> {
                    List<CampaignContactInfoDto> contacts = contactsByCampaignId.getOrDefault(c.getId(), List.of());
                    int totalContacts = contacts.size();
                    int totalCompanies = (int) contacts.stream()
                            .map(CampaignContactInfoDto::getLeadCompanyId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();

                    long sentEmails = 0;
                    long openedEmails = 0;
                    long activeContacts = 0;
                    long terminatedSentEmails = 0;
                    for (CampaignContactInfoDto contact : contacts) {
                        CampaignContactStatus contactStatus = contact.getStatus();
                        boolean isTerminated = contactStatus == CampaignContactStatus.UNSUBSCRIBED
                                || contactStatus == CampaignContactStatus.BOUNCED;
                        if (!isTerminated) {
                            activeContacts++;
                        }
                        List<EmailDataDto> emailData = contact.getEmailData();
                        if (!CollectionUtils.isEmpty(emailData)) {
                            long contactSent = 0;
                            for (EmailDataDto ed : emailData) {
                                if (ed.getEmailDeliveryStatus() != null) {
                                    sentEmails++;
                                    contactSent++;
                                    if (ed.getEmailDeliveryStatus() == EmailDeliveryStatus.OPENED
                                            || ed.getEmailDeliveryStatus() == EmailDeliveryStatus.REPLIED) {
                                        openedEmails++;
                                    }
                                }
                            }
                            if (isTerminated) {
                                terminatedSentEmails += contactSent;
                            }
                        }
                    }

                    int emailSteps = emailStepsByCampaignId.getOrDefault(c.getId(), 0);

                    double progress = 0.0;
                    // Active contacts are expected to receive all email steps.
                    // Terminated contacts (unsubscribed/bounced) only count their already-sent emails as expected.
                    long totalEmails = (activeContacts * emailSteps) + terminatedSentEmails;
                    if (totalEmails > 0) {
                        progress = Math.min(1.0 * sentEmails / totalEmails * 100, 100.0);
                    }

                    return CampaignListDto.fromEntity(
                            c, totalCompanies, totalContacts, sentEmails, openedEmails, progress, emailSteps);
                })
                .toList();

        return new PageImpl<>(content, pageable, campaignsPage.getTotalElements());
    }
}