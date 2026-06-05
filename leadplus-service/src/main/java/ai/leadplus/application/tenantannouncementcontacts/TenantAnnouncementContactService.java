package ai.leadplus.application.tenantannouncementcontacts;

import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.leadcontact.LeadContactDto;
import ai.leadplus.application.leadcontact.LeadContactService;
import ai.leadplus.application.tenantcontacts.TenantContactDto;
import ai.leadplus.application.tenantcontacts.TenantContactService;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContact;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactRepository;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactSource;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactStatus;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncement;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementRepository;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAnnouncementContactService {

    private final TenantAnnouncementRepository tenantAnnouncementRepository;
    private final TenantAnnouncementContactRepository tenantAnnouncementContactRepository;
    private final LeadContactService leadContactService;
    private final TenantContactService tenantContactService;

    public void importLeadContacts(Long tenantId, Long announcementId, List<Long> leadContactIds) {
        TenantAnnouncement announcement = findDraftAnnouncement(tenantId, announcementId);

        List<LeadContactDto> contacts = leadContactService.getLeadContactsByIds(leadContactIds);
        List<String> incomingEmails = contacts.stream()
                .map(LeadContactDto::getEmail)
                .filter(StringUtils::hasText)
                .map(this::normalizeEmail)
                .distinct()
                .toList();

        if (incomingEmails.isEmpty()) {
            log.info("No valid emails found in lead contacts for announcementId: {}", announcementId);
            return;
        }

        Set<String> existingEmails = tenantAnnouncementContactRepository
                .findAllByAnnouncementIdAndEmailIn(announcementId, incomingEmails)
                .stream()
                .map(c -> normalizeEmail(c.getEmail()))
                .collect(Collectors.toSet());

        List<TenantAnnouncementContact> toSave = contacts.stream()
                .filter(c -> c.getEmail() != null && !c.getEmail().isBlank())
                .filter(c -> !existingEmails.contains(normalizeEmail(c.getEmail())))
                .map(c -> TenantAnnouncementContact.builder()
                        .announcementId(announcement.getId())
                        .sourceId(c.getId())
                        .sourceType(TenantAnnouncementContactSource.LEAD)
                        .email(normalizeEmail(c.getEmail()))
                        .firstName(c.getFirstName())
                        .status(TenantAnnouncementContactStatus.PENDING)
                        .build())
                .collect(Collectors.toList());

        tenantAnnouncementContactRepository.saveAll(toSave);
        log.info("Imported {} lead contacts, skipped {} duplicates for announcementId: {}", toSave.size(),
                contacts.size() - toSave.size(), announcementId);
    }

    public void importCrmContacts(Long tenantId, Long announcementId, List<Long> tenantContactIds) {
        TenantAnnouncement announcement = findDraftAnnouncement(tenantId, announcementId);

        List<TenantContactDto> contacts = tenantContactService.getTenantContactsByIds(tenantId, tenantContactIds);
        List<String> incomingEmails = contacts.stream()
                .map(TenantContactDto::getEmail)
                .filter(StringUtils::hasText)
                .map(this::normalizeEmail)
                .distinct()
                .toList();

        if (incomingEmails.isEmpty()) {
            log.info("No valid emails found in CRM contacts for announcementId: {}", announcementId);
            return;
        }

        Set<String> existingEmails = tenantAnnouncementContactRepository
                .findAllByAnnouncementIdAndEmailIn(announcementId, incomingEmails)
                .stream()
                .map(c -> normalizeEmail(c.getEmail()))
                .collect(Collectors.toSet());

        List<TenantAnnouncementContact> toSave = contacts.stream()
                .filter(c -> c.getEmail() != null && !c.getEmail().isBlank())
                .filter(c -> !existingEmails.contains(normalizeEmail(c.getEmail())))
                .map(c -> TenantAnnouncementContact.builder()
                        .announcementId(announcement.getId())
                        .sourceId(c.getId())
                        .sourceType(TenantAnnouncementContactSource.CRM)
                        .email(normalizeEmail(c.getEmail()))
                        .firstName(c.getFirstName())
                        .status(TenantAnnouncementContactStatus.PENDING)
                        .build())
                .collect(Collectors.toList());

        tenantAnnouncementContactRepository.saveAll(toSave);
        log.info("Imported {} CRM contacts, skipped {} duplicates for announcementId: {}", toSave.size(), contacts.size() - toSave.size(), announcementId);
    }

    public Page<TenantAnnouncementContactDto> listContacts(Long tenantId, Long announcementId, Pageable pageable) {
        findAnnouncementByTenant(tenantId, announcementId);
        return tenantAnnouncementContactRepository.findAllByAnnouncementId(announcementId, pageable)
                .map(TenantAnnouncementContactDto::fromEntity);
    }

    public void removeContact(Long tenantId, Long announcementId, Long contactId) {
        checkDraftAnnouncement(tenantId, announcementId);
        TenantAnnouncementContact contact = tenantAnnouncementContactRepository.findByIdAndAnnouncementId(contactId, announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));
        tenantAnnouncementContactRepository.delete(contact);
    }

    public void clearContacts(Long tenantId, Long announcementId) {
        checkDraftAnnouncement(tenantId, announcementId);
        tenantAnnouncementContactRepository.deleteAllByAnnouncementId(announcementId);
        log.info("Cleared all contacts for announcementId: {}", announcementId);
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase().trim();
    }

    private TenantAnnouncement findDraftAnnouncement(Long tenantId, Long announcementId) {
        TenantAnnouncement announcement = findAnnouncementByTenant(tenantId, announcementId);
        if (announcement.getStatus() != TenantAnnouncementStatus.DRAFT) {
            throw new BadRequestException("Operation only allowed on DRAFT announcements");
        }
        return announcement;
    }

    private void checkDraftAnnouncement(Long tenantId, Long announcementId) {
        TenantAnnouncement announcement = findAnnouncementByTenant(tenantId, announcementId);
        if (announcement.getStatus() != TenantAnnouncementStatus.DRAFT) {
            throw new BadRequestException("Operation only allowed on DRAFT announcements");
        }
    }

    private TenantAnnouncement findAnnouncementByTenant(Long tenantId, Long announcementId) {
        return tenantAnnouncementRepository.findByIdAndTenantIdAndActiveTrue(announcementId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));
    }
}
