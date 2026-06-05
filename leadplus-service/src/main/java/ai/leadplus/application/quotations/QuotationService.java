package ai.leadplus.application.quotations;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.requestforquotes.CreateRequestForQuoteEvent;
import ai.leadplus.application.requestforquotes.RequestForQuoteDto;
import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.domain.quotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationService {

    private static final List<QuotationStatus> QUOTATION_COUNTABLE_STATUSES = List.of(
            QuotationStatus.ACCEPTED,
            QuotationStatus.QUOTED
    );

    private final QuotationRepository quotationRepository;

    public QuotationDto createQuotation(QuotationDto dto, SourcingRequestType sourceType) {
        dto.setStatus(QuotationStatus.PENDING);
        dto.setActive(true);
        dto.setSourceType(sourceType);
        Quotation saved = quotationRepository.save(dto.toEntity());
        return QuotationDto.fromEntity(saved);
    }

    public QuotationDto getQuotationById(String id) {
        return QuotationDto.fromEntity(findActiveById(id));
    }

    public QuotationDto getQuotationByIdAndSourceType(String id, SourcingRequestType sourceType) {
        return QuotationDto.fromEntity(findActiveByIdAndSourceType(id, sourceType));
    }

    public Page<QuotationDto> getAllByVendorIdAndSourceType(Long vendorId, SourcingRequestType sourceType, Pageable pageable) {
        return quotationRepository.findAllByVendorIdAndSourceTypeAndActiveTrue(vendorId, sourceType, pageable)
                .map(QuotationDto::fromEntity);
    }

    public Page<QuotationDto> getAllBySourceIdAndSourceType(Long sourceId,
                                                            SourcingRequestType sourceType,
                                                            Pageable pageable) {
        return quotationRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(sourceId, sourceType, pageable)
                .map(QuotationDto::fromEntity);
    }

    public List<QuotationDto> getAllBySource(Long sourceId, SourcingRequestType sourceType) {
        return quotationRepository
                .findAllBySourceIdAndSourceTypeAndActiveTrue(sourceId, sourceType)
                .stream()
                .map(QuotationDto::fromEntity)
                .toList();
    }

    public QuotationDto updateQuotation(Long id, QuotationDto dto, SourcingRequestType sourceType) {
        Quotation existing = findByIdAndVendorIdAndSourceTypeAndActiveTrue(id, dto.getVendorId(), sourceType);

        List<QuotationItem> items = CollectionUtils.isEmpty(dto.getItems()) ?
                List.of() :
                dto.getItems().stream()
                        .map(QuotationItemDto::toEntity)
                        .toList();
        existing.setItems(items);
        existing.setPaymentTerms(dto.getPaymentTerms());
        existing.setDeliverables(dto.getDeliverables());

        return QuotationDto.fromEntity(quotationRepository.save(existing));
    }

    public QuotationDto updateQuotationStatus(Long id, Long vendorId, QuotationStatus status, SourcingRequestType sourceType) {
        Quotation quotation = findByIdAndVendorIdAndSourceTypeAndActiveTrue(id, vendorId, sourceType);
        quotation.setStatus(status);
        return QuotationDto.fromEntity(quotationRepository.save(quotation));
    }

    public void deleteQuotation(Long id, Long vendorId, SourcingRequestType sourceType) {
        Quotation quotation = findByIdAndVendorIdAndSourceTypeAndActiveTrue(id, vendorId, sourceType);
        quotation.setActive(false);
        quotationRepository.save(quotation);
    }

    public int getCountOfQuotationsForSource(Long sourceId, SourcingRequestType sourceType) {
        return quotationRepository.countBySourceIdAndSourceTypeAndStatusInAndActiveTrue(sourceId, sourceType, QUOTATION_COUNTABLE_STATUSES);
    }

    @Async
    @TransactionalEventListener
    public void handleCreateRequestForQuoteEvent(CreateRequestForQuoteEvent event) {
        RequestForQuoteDto requestForQuoteDto = event.getRequestForQuoteDto();
        Quotation quotation = Quotation.builder()
                .sourceId(requestForQuoteDto.getId())
                .title(requestForQuoteDto.getTitle())
                .description(requestForQuoteDto.getDescription())
                .budget(requestForQuoteDto.getBudget())
                .deadline(requestForQuoteDto.getDeadline())
                .status(QuotationStatus.PENDING)
                .sourceType(SourcingRequestType.REQUEST_FOR_QUOTE)
                .vendorId(event.getVendorId())
                .active(true)
                .build();
        quotationRepository.save(quotation);
    }

    private Quotation findByIdAndVendorIdAndSourceTypeAndActiveTrue(Long id, Long vendorId, SourcingRequestType sourceType) {
        return quotationRepository.findByIdAndVendorIdAndSourceTypeAndActiveTrue(id, vendorId, sourceType)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found with id: " + id + " and vendorId: " + vendorId + " and sourceType: " + sourceType.toString()));
    }

    private Quotation findActiveById(String id) {
        return quotationRepository.findByIdAndActiveTrue(Long.parseLong(id))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quotation not found with id: " + id));
    }

    private Quotation findActiveByIdAndSourceType(String id, SourcingRequestType sourceType) {
        return quotationRepository.findByIdAndSourceTypeAndActiveTrue(Long.parseLong(id), sourceType)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Quotation not found with id: " + id));
    }
}