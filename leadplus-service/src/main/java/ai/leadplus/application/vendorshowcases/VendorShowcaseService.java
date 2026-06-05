package ai.leadplus.application.vendorshowcases;

import ai.leadplus.application.attachments.AttachmentDto;
import ai.leadplus.application.attachments.AttachmentService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.domain.attachments.SourceType;
import ai.leadplus.domain.vendorshowcases.VendorShowcase;
import ai.leadplus.domain.vendorshowcases.VendorShowcaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VendorShowcaseService {

    private final VendorShowcaseRepository vendorShowcaseRepository;
    private final ServiceCatalogueService serviceCatalogueService;
    private final AttachmentService attachmentService;

    public VendorShowcaseWithAttachmentsDto getVendorShowcaseById(Long id) {
        VendorShowcase vendorShowcase = findShowcaseById(id);
        return mapToAttachmentsDto(vendorShowcase);
    }

    private VendorShowcase findShowcaseById(Long id) {
        return vendorShowcaseRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor Showcase not found with id: " + id));
    }

    public List<VendorShowcaseWithAttachmentsDto> getVendorShowcasesByVendorId(Long vendorId) {
        List<VendorShowcase> showcases = vendorShowcaseRepository.findAllByVendorIdAndActiveTrue(vendorId);
        List<Long> showcaseIds = showcases.stream()
                .map(VendorShowcase::getId)
                .toList();

        Map<Long, List<AttachmentDto>> attachmentsMap = attachmentService.getAttachmentsBySourceIdsAndSourceType(showcaseIds, SourceType.SHOWCASE);
        return showcases.stream()
                .map(showcase -> VendorShowcaseWithAttachmentsDto.fromEntity(
                        showcase, attachmentsMap.getOrDefault(showcase.getId(), List.of()))
                )
                .toList();
    }

    public VendorShowcaseDto createVendorShowcase(VendorShowcaseDto vendorShowcaseDto) {
        if (!CollectionUtils.isEmpty(vendorShowcaseDto.getServiceIds())) {
            serviceCatalogueService.validateServiceIds(vendorShowcaseDto.getServiceIds());
        }
        VendorShowcase vendorShowcase = vendorShowcaseDto.toEntity();
        vendorShowcase.setActive(true);
        vendorShowcaseRepository.save(vendorShowcase);
        return VendorShowcaseDto.fromEntity(vendorShowcase);
    }

    public VendorShowcaseWithAttachmentsDto updateShowcase(Long  showcaseId, VendorShowcaseDto vendorShowcaseDto) {
        VendorShowcase existing = findShowcaseById(showcaseId);

        if (!Objects.equals(existing.getServiceIds(), vendorShowcaseDto.getServiceIds())) {
            if (!CollectionUtils.isEmpty(vendorShowcaseDto.getServiceIds())) {
                serviceCatalogueService.validateServiceIds(vendorShowcaseDto.getServiceIds());
            }
            existing.setServiceIds(vendorShowcaseDto.getServiceIds());
        }
        existing.setProjectName(vendorShowcaseDto.getProjectName());
        existing.setClientName(vendorShowcaseDto.getClientName());
        existing.setDescription(vendorShowcaseDto.getDescription());
        existing.setDuration(vendorShowcaseDto.getDuration());
        existing.setResultsAndOutcomes(vendorShowcaseDto.getResultsAndOutcomes());
        vendorShowcaseRepository.save(existing);
        return mapToAttachmentsDto(existing);
    }

    public VendorShowcaseWithAttachmentsDto uploadShowcaseFile(Long showcaseId, MultipartFile file) {
        VendorShowcase vendorShowcase = findShowcaseById(showcaseId);
        attachmentService.createAttachment(showcaseId, SourceType.SHOWCASE, file);
        return mapToAttachmentsDto(vendorShowcase);
    }

    public VendorShowcaseWithAttachmentsDto deleteShowcaseFile(Long showcaseId, String attachmentId) {
        VendorShowcase vendorShowcase = findShowcaseById(showcaseId);
        attachmentService.deleteAttachment(attachmentId, SourceType.SHOWCASE);
        return mapToAttachmentsDto(vendorShowcase);
    }

    public void deleteVendorShowcase(Long id) {
        VendorShowcase vendorShowcase = findShowcaseById(id);
        vendorShowcase.setActive(false);
        vendorShowcaseRepository.save(vendorShowcase);
        attachmentService.deleteAttachmentsBySourceIdAndSourceType(id, SourceType.SHOWCASE);
    }

    private VendorShowcaseWithAttachmentsDto mapToAttachmentsDto(VendorShowcase vendorShowcase) {
        List<AttachmentDto> attachments = attachmentService.getAttachments(vendorShowcase.getId(), SourceType.SHOWCASE);
        return VendorShowcaseWithAttachmentsDto.fromEntity(vendorShowcase, attachments);
    }

}
