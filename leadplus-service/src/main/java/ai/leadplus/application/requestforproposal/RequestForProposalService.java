package ai.leadplus.application.requestforproposal;

import ai.leadplus.application.attachments.AttachmentDto;
import ai.leadplus.application.attachments.AttachmentService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.application.specifications.SpecificationService;
import ai.leadplus.domain.attachments.SourceType;
import ai.leadplus.domain.common.RequestStatus;
import ai.leadplus.domain.requestforproposal.RequestForProposal;
import ai.leadplus.domain.requestforproposal.RequestForProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestForProposalService {

    private final RequestForProposalRepository requestForProposalRepository;
    private final ServiceCatalogueService serviceCatalogueService;
    private final SpecificationService specificationService;
    private final AttachmentService attachmentService;

    public RequestForProposalDto createRequestForProposal(RequestForProposalDto dto) {
        serviceCatalogueService.validateServiceIds(dto.getServiceIds());
        specificationService.validateSpecificationIds(dto.getSpecificationIds());
        dto.setActive(true);
        dto.setStatus(RequestStatus.OPEN);
        return RequestForProposalDto.fromEntity(requestForProposalRepository.save(dto.toEntity()));
    }

    public RequestForProposalWithAttachmentsDto getRequestForProposalById(Long id) {
        RequestForProposal requestForProposal = findActiveById(id);
        return mapToAttachmentsDto(requestForProposal);
    }

    public Page<RequestForProposalWithAttachmentsDto> getAllRequestForProposalsByUser(Long userId, Pageable pageable) {
        return requestForProposalRepository.findAllByUserIdAndActiveTrue(userId, pageable)
                .map(this::mapToAttachmentsDto);
    }

    public RequestForProposalWithAttachmentsDto updateRequestForProposal(Long id, RequestForProposalDto dto) {
        RequestForProposal existing = findActiveById(id);

        if (!existing.getServiceIds().equals(dto.getServiceIds())) {
            serviceCatalogueService.validateServiceIds(dto.getServiceIds());
        }
        if(!existing.getSpecificationIds().equals(dto.getSpecificationIds())){
            specificationService.validateSpecificationIds(dto.getSpecificationIds());
        }

        existing.setTitle(dto.getTitle());
        existing.setServiceIds(dto.getServiceIds());
        existing.setQuantity(dto.getQuantity());
        existing.setBudget(dto.getBudget());
        existing.setTimeline(dto.getTimeline());
        existing.setDescription(dto.getDescription());
        existing.setSpecificationIds(dto.getSpecificationIds());

        RequestForProposal requestForProposal = requestForProposalRepository.save(existing);
        return mapToAttachmentsDto(requestForProposal);
    }

    public RequestForProposalWithAttachmentsDto uploadAttachment(Long id, MultipartFile file) {
        RequestForProposal requestForProposal = findActiveById(id);
        attachmentService.createAttachment(id, SourceType.REQUEST_FOR_PROPOSAL, file);
        return mapToAttachmentsDto(requestForProposal);
    }

    public RequestForProposalWithAttachmentsDto deleteAttachment(Long id, String attachmentId) {
        RequestForProposal requestForProposal = findActiveById(id);
        attachmentService.deleteAttachment(attachmentId, SourceType.REQUEST_FOR_PROPOSAL);
        return mapToAttachmentsDto(requestForProposal);
    }

    public void deleteRequestForProposal(Long id) {
        RequestForProposal existing = findActiveById(id);
        existing.setActive(false);
        requestForProposalRepository.save(existing);
        attachmentService.deleteAttachmentsBySourceIdAndSourceType(id, SourceType.REQUEST_FOR_PROPOSAL);
    }

    private RequestForProposal findActiveById(Long id) {
        return requestForProposalRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFP not found: " + id));
    }

    private RequestForProposalWithAttachmentsDto mapToAttachmentsDto(RequestForProposal requestForProposal) {
        List<AttachmentDto> attachments = attachmentService.getAttachments(requestForProposal.getId(), SourceType.REQUEST_FOR_PROPOSAL);
        return RequestForProposalWithAttachmentsDto.fromEntity(requestForProposal, attachments);
    }
}