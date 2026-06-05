package ai.leadplus.application.announcementorchestrator;

import ai.leadplus.application.attachmentlibraries.AttachmentLibraryDto;
import ai.leadplus.application.attachmentlibraries.AttachmentLibraryService;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.exception.InvalidEmailAddressException;
import ai.leadplus.application.leadcompany.LeadCompanyService;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.tenants.TenantDto;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.domain.common.CRM;
import ai.leadplus.domain.common.Recipient;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContact;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactRepository;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactSource;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactStatus;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncement;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementRepository;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementStatus;
import ai.leadplus.domain.tenantcompanies.TenantCompanyRepository;
import ai.leadplus.domain.tenantcontacts.TenantContact;
import ai.leadplus.domain.tenantcontacts.TenantContactRepository;
import ai.leadplus.infrastructure.springmail.SpringMailClient;
import ai.leadplus.infrastructure.springmail.SpringMailRequest;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAnnouncementOrchestratorService {

    @Value("${mail-guard.enabled:true}")
    private boolean isMailGuardEnabled;

    @Value("${announcement.orchestration.scheduler.throttle-per-hour:20}")
    private int throttlePerHour;

    private static final String GUARD_EMAIL = "nethma+campaign@limarktech.com";

    private final TenantAnnouncementRepository tenantAnnouncementRepository;
    private final TenantAnnouncementContactRepository tenantAnnouncementContactRepository;
    private final LeadContactService leadContactService;
    private final LeadCompanyService leadCompanyService;
    private final TenantContactRepository tenantContactRepository;
    private final TenantCompanyRepository tenantCompanyRepository;
    private final TenantService tenantService;
    private final SpringMailClient springMailClient;
    private final AttachmentLibraryService attachmentLibraryService;

    @Value("${aws.cloudfront.url}")
    private String cloudfrontUrl;

    /**
     * Get all in-progress announcements, group them by tenant, and process them per tenant.
     * This way, the hourly send limit is shared across all active announcements of a tenant (not per announcement).
     */
    public void processAnnouncements() {
        tenantAnnouncementRepository.findAllByStatusAndActiveTrue(TenantAnnouncementStatus.IN_PROGRESS)
                .stream()
                .collect(Collectors.groupingBy(TenantAnnouncement::getTenantId))
                .forEach(this::processAnnouncementsForTenant);
    }

    private void processAnnouncementsForTenant(Long tenantId, List<TenantAnnouncement> announcements) {
        TenantDto tenant = tenantService.getTenant(tenantId);

        String fromEmail = tenant.getAnnouncementFromEmail();
        String smtpAppPassword = tenant.getAnnouncementMetaData() != null
                ? tenant.getAnnouncementMetaData().getSmtpAppPassword()
                : null;

        if (fromEmail == null || smtpAppPassword == null) {
            log.warn("Skipping announcements for tenant {} - missing SMTP credentials", tenantId);
            return;
        }

        List<Long> announcementIds = announcements.stream()
                 .map(TenantAnnouncement::getId)
                .collect(Collectors.toList());

        int remainingQuota = getRemainingHourlyQuota(announcementIds);
        if (remainingQuota == 0) {
            log.info("From Email {} — hourly quota used, skipping", fromEmail);
            return;
        }

        for (TenantAnnouncement announcement : announcements) {
            if (remainingQuota <= 0) break;

            List<TenantAnnouncementContact> contacts = tenantAnnouncementContactRepository
                    .findAllByAnnouncementIdAndStatus(announcement.getId(), TenantAnnouncementContactStatus.PENDING, PageRequest.of(0, remainingQuota))
                    .getContent();

            Map<String, String> companyNames = buildCompanyNameMap(tenantId, contacts);

            for (TenantAnnouncementContact contact : contacts) {
                if (remainingQuota <= 0) break;
                try {
                    String toEmail = isMailGuardEnabled ? GUARD_EMAIL : contact.getEmail();
                    String companyName = companyNames.getOrDefault(contact.getSourceId(), "");
                    String subject = replacePlaceholders(announcement.getSubject(), contact, companyName);
                    String body = replacePlaceholders(announcement.getBody(), contact, companyName);
                    SpringMailRequest request = buildMailRequest(announcement, tenant, toEmail, subject, body);
                    springMailClient.sendEmail(request, fromEmail, smtpAppPassword);
                    contact.setStatus(TenantAnnouncementContactStatus.SENT);
                    contact.setSentAt(LocalDateTime.now());
                    remainingQuota--;
                } catch (Exception e) {
                    log.error("Failed to send announcement email to contact {}", contact.getId(), e);
                    contact.setStatus(TenantAnnouncementContactStatus.FAILED);
                }
            }

            tenantAnnouncementContactRepository.saveAll(contacts);

            long remainingPending = tenantAnnouncementContactRepository.countByAnnouncementIdAndStatus(announcement.getId(), TenantAnnouncementContactStatus.PENDING);
            if (remainingPending == 0) {
                announcement.setStatus(TenantAnnouncementStatus.COMPLETED);
                tenantAnnouncementRepository.save(announcement);
                log.info("Announcement {} completed", announcement.getId());
            }
        }
    }

    /**
     * Returns the remaining hourly send quota for the given announcements.
     * If the limit is reached, returns 0.
     */
    private int getRemainingHourlyQuota(List<Long> announcementIds) {
        long used = tenantAnnouncementContactRepository.countByAnnouncementIdInAndSentAtAfter(announcementIds, LocalDateTime.now().minusHours(1));
        return Math.max(0, (int) (throttlePerHour - used));
    }

    private Map<String, String> buildCompanyNameMap(Long tenantId, List<TenantAnnouncementContact> contacts) {
        Map<String, String> result = new HashMap<>();

        List<Long> leadSourceIds = contacts.stream()
                .filter(c -> c.getSourceType() == TenantAnnouncementContactSource.LEAD)
                .map(TenantAnnouncementContact::getSourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!leadSourceIds.isEmpty()) {
            List<LeadContactDto> leadContacts = leadContactService.getLeadContactsByIds(leadSourceIds);
            List<Long> companyIds = leadContacts.stream()
                    .map(LeadContactDto::getLeadCompanyId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, String> companyNameById = new HashMap<>();
            if (!companyIds.isEmpty()) {
                leadCompanyService.getLeadCompaniesByIds(companyIds)
                        .forEach(c -> companyNameById.put(String.valueOf(c.getId()), c.getName()));
            }

            leadContacts.forEach(lc -> {
                String name = lc.getLeadCompanyId() != null
                        ? companyNameById.getOrDefault(lc.getLeadCompanyId(), "")
                        : "";
                result.put(String.valueOf(lc.getId()), name);
            });
        }

        List<Long> crmSourceIds = contacts.stream()
                .filter(c -> c.getSourceType() == TenantAnnouncementContactSource.CRM)
                .map(TenantAnnouncementContact::getSourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!crmSourceIds.isEmpty()) {
            List<TenantContact> crmContacts = tenantContactRepository.findAllByTenantIdAndIdIn(tenantId, crmSourceIds);

            Map<CRM, List<String>> companyIdsBySourceType = crmContacts.stream()
                    .filter(c -> c.getCompanyId() != null)
                    .collect(Collectors.groupingBy(
                            TenantContact::getSourceType,
                            Collectors.mapping(TenantContact::getCompanyId, Collectors.toList())
                    ));

            Map<String, String> tenantCompanyNameMap = new HashMap<>();
            companyIdsBySourceType.forEach((sourceType, companyIds) ->
                    tenantCompanyRepository.findAllByTenantIdAndSourceTypeAndSourceIdIn(tenantId, sourceType, companyIds)
                            .forEach(tc -> tenantCompanyNameMap.put(sourceType + ":" + tc.getSourceId(), tc.getName()))
            );

            crmContacts.forEach(tc -> {
                String name = tc.getCompanyId() != null
                        ? tenantCompanyNameMap.getOrDefault(tc.getSourceType() + ":" + tc.getCompanyId(), "")
                        : "";
                result.put(String.valueOf(tc.getId()), name);
            });
        }

        return result;
    }

    private String replacePlaceholders(String template, TenantAnnouncementContact contact, String companyName) {
        if (template == null) return "";
        String firstName = StringUtils.hasText(contact.getFirstName()) ? contact.getFirstName() : "";
        String result = template;
        result = result.replace("{firstName}", firstName);
        result = result.replace("{companyName}", companyName != null ? companyName : "");
        return result;
    }

    private SpringMailRequest buildMailRequest(TenantAnnouncement announcement, TenantDto tenant, String toEmail, String subject, String body) {
        try {
            InternetAddress fromAddress = new InternetAddress(tenant.getAnnouncementFromEmail(), tenant.getAnnouncementSenderName());
            InternetAddress[] toAddresses = new InternetAddress[]{new InternetAddress(toEmail)};
            InternetAddress[] ccAddresses = toInternetAddresses(announcement.getCcRecipients());
            InternetAddress[] bccAddresses = toInternetAddresses(announcement.getBccRecipients());
            List<SpringMailRequest.Attachment> attachments = buildAttachments(announcement.getAttachmentIds());
            return SpringMailRequest.builder()
                    .subject(subject)
                    .body(body)
                    .fromAddress(fromAddress)
                    .toAddresses(toAddresses)
                    .ccAddresses(ccAddresses.length > 0 ? ccAddresses : null)
                    .bccAddresses(bccAddresses.length > 0 ? bccAddresses : null)
                    .attachments(attachments.isEmpty() ? null : attachments)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build mail request", e);
        }
    }

    private List<SpringMailRequest.Attachment> buildAttachments(List<String> attachmentIds) {
        if (CollectionUtils.isEmpty(attachmentIds)) {
            return List.of();
        }
        return attachmentLibraryService.getAttachmentsByIds(attachmentIds).stream()
                .map(this::convertToSpringAttachment)
                .toList();
    }

    private SpringMailRequest.Attachment convertToSpringAttachment(AttachmentLibraryDto dto) {
        String fileUrl = cloudfrontUrl + dto.getFileUrl();
        byte[] contentBytes = FileReader.convertUrlToBytes(fileUrl);
        String fullFilename = dto.getFilename() + "." + dto.getFileType();
        String mimeType = URLConnection.guessContentTypeFromName(fullFilename);
        if (mimeType == null) mimeType = "application/octet-stream";
        return SpringMailRequest.Attachment.builder()
                .fileName(fullFilename)
                .resource(new ByteArrayResource(contentBytes))
                .contentType(mimeType)
                .build();
    }

    private InternetAddress[] toInternetAddresses(List<Recipient> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return new InternetAddress[0];
        }
        return recipients.stream()
                .map(r -> {
                    try {
                        return new InternetAddress(r.getEmail(), r.getName());
                    } catch (UnsupportedEncodingException e) {
                        throw new InvalidEmailAddressException(r.getEmail());
                    }
                })
                .toArray(InternetAddress[]::new);
    }
}
