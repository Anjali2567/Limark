package ai.leadplus.application.tenantannouncements;

import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncement;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementRepository;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementStatus;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAnnouncementService {

    private final TenantAnnouncementRepository tenantAnnouncementRepository;
    private final TenantAnnouncementContactRepository tenantAnnouncementContactRepository;

    public TenantAnnouncementDto createAnnouncement(Long tenantId, TenantAnnouncementDto dto) {
        TenantAnnouncement tenantAnnouncement = dto.toEntity();
        tenantAnnouncement.setTenantId(tenantId);
        tenantAnnouncement.setStatus(TenantAnnouncementStatus.DRAFT);
        tenantAnnouncement.setActive(true);
        return TenantAnnouncementDto.fromEntity(tenantAnnouncementRepository.save(tenantAnnouncement));
    }

    public Page<TenantAnnouncementDto> listAnnouncements(Long tenantId, String query, Pageable pageable) {
        Page<TenantAnnouncement> page = StringUtils.hasText(query)
                ? tenantAnnouncementRepository.findAllByTenantIdAndNameContainingIgnoreCaseAndActiveTrue(tenantId, query, pageable)
                : tenantAnnouncementRepository.findAllByTenantIdAndActiveTrue(tenantId, pageable);
        return page.map(announcement -> {
            TenantAnnouncementDto dto = TenantAnnouncementDto.fromEntity(announcement);
            dto.setRecipientCount(tenantAnnouncementContactRepository.countByAnnouncementId(announcement.getId() ));
            return dto;
        });
    }

    public TenantAnnouncementDto getAnnouncement(Long tenantId, Long announcementId) {
        return TenantAnnouncementDto.fromEntity(findByIdAndTenantId(tenantId, announcementId));
    }

    public TenantAnnouncementDto updateAnnouncement(Long tenantId, Long announcementId, TenantAnnouncementDto dto) {
        TenantAnnouncement tenantAnnouncement = findByIdAndTenantId(tenantId, announcementId);
        if (tenantAnnouncement.getStatus() != TenantAnnouncementStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT announcements can be updated");
        }
        tenantAnnouncement.setName(dto.getName());
        tenantAnnouncement.setSubject(dto.getSubject());
        tenantAnnouncement.setBody(dto.getBody());
        tenantAnnouncement.setCcRecipients(RecipientUtils.mapToEntity(dto.getCcRecipients()));
        tenantAnnouncement.setBccRecipients(RecipientUtils.mapToEntity(dto.getBccRecipients()));
        tenantAnnouncement.setAttachmentIds(dto.getAttachmentIds());
        return TenantAnnouncementDto.fromEntity(tenantAnnouncementRepository.save(tenantAnnouncement));
    }

    public void deleteAnnouncement(Long tenantId, Long announcementId) {
        TenantAnnouncement tenantAnnouncement = findByIdAndTenantId(tenantId, announcementId);
        if (tenantAnnouncement.getStatus() != TenantAnnouncementStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT announcements can be deleted");
        }
        tenantAnnouncement.setActive(false);
        tenantAnnouncementRepository.save(tenantAnnouncement);
    }

    public TenantAnnouncementDto launchAnnouncement(Long tenantId, Long announcementId) {
        TenantAnnouncement announcement = findByIdAndTenantId(tenantId, announcementId);
        if (announcement.getStatus() != TenantAnnouncementStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT announcements can be launched");
        }
        if (tenantAnnouncementContactRepository.countByAnnouncementId(announcementId) == 0) {
            throw new BadRequestException("No contacts selected to send announcement");
        }
        announcement.setStatus(TenantAnnouncementStatus.IN_PROGRESS);
        announcement.setLaunchedAt(LocalDateTime.now());
        return TenantAnnouncementDto.fromEntity(tenantAnnouncementRepository.save(announcement));
    }

    private TenantAnnouncement findByIdAndTenantId(Long tenantId, Long announcementId) {
        return tenantAnnouncementRepository.findByIdAndTenantIdAndActiveTrue(announcementId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));
    }
}
