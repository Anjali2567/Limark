package ai.leadplus.application.requestforquotes;

import ai.leadplus.application.attachments.AttachmentDto;
import ai.leadplus.application.attachments.AttachmentService;
import ai.leadplus.application.collaborators.CollaboratorService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.services.ServiceDto;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.domain.attachments.SourceType;
import ai.leadplus.domain.common.RequestStatus;
import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.domain.requestforquotes.RequestForQuote;
import ai.leadplus.domain.requestforquotes.RequestForQuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestForQuoteService {

    private final RequestForQuoteRepository requestForQuoteRepository;
    private final ServiceCatalogueService serviceCatalogueService;
    private final AttachmentService attachmentService;
    private final VendorService vendorService;
    private final CollaboratorService collaboratorService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RequestForQuoteDto createRequestForQuote(RequestForQuoteDto dto) {

        serviceCatalogueService.validateServiceIds(dto.getServiceIds());
        vendorService.validateVendorIds(dto.getVendorIds());

        RequestForQuote entity = dto.toEntity();
        entity.setActive(true);
        entity.setStatus(RequestStatus.OPEN);

        RequestForQuote saved = requestForQuoteRepository.save(entity);

        collaboratorService.createOwner(saved.getId(), SourcingRequestType.REQUEST_FOR_QUOTE, saved.getUserId());
        RequestForQuoteDto requestForQuoteDto = RequestForQuoteDto.fromEntity(saved);

        saved.getVendorIds().forEach(vendorId ->
                eventPublisher.publishEvent(
                        new CreateRequestForQuoteEvent(this, requestForQuoteDto, vendorId)
                )
        );

        return requestForQuoteDto;
    }

    public RequestForQuoteDetailedDto getRequestForQuoteById(Long id) {
        RequestForQuote requestForQuote = findActiveById(id);
        return mapToAttachmentsDto(requestForQuote);
    }

    public Page<RequestForQuoteDetailedDto> getAllRequestForQuotesByUser(Long userId, Pageable pageable) {
        Page<RequestForQuote> requestForQuotes = requestForQuoteRepository.findAllByUserIdAndActiveTrue(userId, pageable);
        List<Long> serviceIds = requestForQuotes.stream()
                .flatMap(rfq -> rfq.getServiceIds().stream())
                .distinct()
                .toList();
        Map<Long, ServiceDto> serviceMap = serviceCatalogueService.getAllServicesAsMap(serviceIds);
        Page<RequestForQuoteDetailedDto> requestForQuoteDetailedDtoPage = requestForQuotes.map(this::mapToAttachmentsDto);
        requestForQuoteDetailedDtoPage.forEach(dto -> {
            if (!CollectionUtils.isEmpty(dto.getServiceIds())) {
                List<ServiceDto> services = dto.getServiceIds().stream()
                        .map(serviceMap::get)
                        .toList();
                dto.setServices(services);
            }
        });
        return requestForQuoteDetailedDtoPage;
    }

    public Page<RequestForQuoteDetailedDto> getAllRequestForQuoteByVendor(Long vendorId, Pageable pageable) {
        return requestForQuoteRepository.findAllByVendorIdsContainingAndActiveTrue(vendorId, pageable)
                .map(this::mapToAttachmentsDto);
    }

    public RequestForQuoteDetailedDto updateRequestForQuote(Long id, RequestForQuoteDto dto) {
        RequestForQuote existing = findActiveById(id);

        if (!existing.getServiceIds().equals(dto.getServiceIds())) {
            serviceCatalogueService.validateServiceIds(dto.getServiceIds());
        }
        if (!existing.getVendorIds().equals(dto.getVendorIds())) {
            vendorService.areVendorsActive(dto.getVendorIds());
        }

        existing.setTitle(dto.getTitle());
        existing.setServiceIds(dto.getServiceIds());
        existing.setVendorIds(dto.getVendorIds());
        existing.setQuantity(dto.getQuantity());
        existing.setBudget(dto.getBudget());
        existing.setDeadline(dto.getDeadline());
        existing.setDescription(dto.getDescription());
        RequestForQuote requestForQuote = requestForQuoteRepository.save(existing);
        return mapToAttachmentsDto(requestForQuote);
    }

    public RequestForQuoteDetailedDto uploadAttachment(Long id, MultipartFile file) {
        RequestForQuote requestForQuote = findActiveById(id);
        attachmentService.createAttachment(id, SourceType.REQUEST_FOR_QUOTE, file);
        return mapToAttachmentsDto(requestForQuote);
    }

    public RequestForQuoteDetailedDto deleteAttachment(Long id, String attachmentId) {
        RequestForQuote requestForQuote = findActiveById(id);
        attachmentService.deleteAttachment(attachmentId, SourceType.REQUEST_FOR_QUOTE);
        return mapToAttachmentsDto(requestForQuote);
    }

    public void deleteRequestForQuote(Long id) {
        RequestForQuote existing = findActiveById(id);
        existing.setActive(false);
        requestForQuoteRepository.save(existing);
        attachmentService.deleteAttachmentsBySourceIdAndSourceType(id, SourceType.REQUEST_FOR_QUOTE);
    }

    private RequestForQuote findActiveById(Long id) {
        return requestForQuoteRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found: " + id));
    }

    private RequestForQuoteDetailedDto mapToAttachmentsDto(RequestForQuote requestForQuote) {
        List<AttachmentDto> attachments = attachmentService.getAttachments(requestForQuote.getId(), SourceType.REQUEST_FOR_QUOTE);
        return RequestForQuoteDetailedDto.fromEntity(requestForQuote, attachments);
    }
}
