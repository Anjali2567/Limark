package ai.leadplus.application.requestforquotes;

import ai.leadplus.application.attachments.AttachmentService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.services.ServiceDto;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.application.collaborators.CollaboratorService;
import ai.leadplus.domain.attachments.SourceType;
import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.common.RequestStatus;
import ai.leadplus.domain.requestforquotes.RequestForQuote;
import ai.leadplus.domain.requestforquotes.RequestForQuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RequestForQuoteServiceTest {

    @Mock
    private RequestForQuoteRepository repository;

    @Mock
    private ServiceCatalogueService serviceCatalogueService;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private VendorService vendorService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CollaboratorService collaboratorService;

    @InjectMocks
    private RequestForQuoteService service;

    private RequestForQuote rfq;

    @BeforeEach
    void setup() {
        rfq = RequestForQuote.builder()
                .id(1L)
                .title("Test RFQ")
                .serviceIds(List.of(1L))
                .vendorIds(List.of(1L))
                .quantity(10)
                .budget(BudgetRange.ABOVE_50K)
                .deadline(LocalDate.now().plusDays(7).atStartOfDay())
                .description("desc")
                .status(RequestStatus.OPEN)
                .active(true)
                .build();
    }

    @Test
    void createRequestForQuote_shouldSaveAndPublishEvents() {
        RequestForQuoteDto dto = RequestForQuoteDto.fromEntity(rfq);

        when(repository.save(any())).thenReturn(rfq);

        RequestForQuoteDto result = service.createRequestForQuote(dto);

        verify(serviceCatalogueService).validateServiceIds(dto.getServiceIds());
        verify(vendorService).validateVendorIds(dto.getVendorIds());
        verify(repository).save(any());
        verify(eventPublisher, times(rfq.getVendorIds().size())).publishEvent(any());
        assertEquals(1L, result.getId());
        assertEquals(RequestStatus.OPEN, result.getStatus());
    }

    @Test
    void getRequestForQuoteById_shouldReturnDetailedDto() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(rfq));
        when(attachmentService.getAttachments(1L, SourceType.REQUEST_FOR_QUOTE))
                .thenReturn(List.of());

        RequestForQuoteDetailedDto result = service.getRequestForQuoteById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getRequestForQuoteById_shouldThrowIfNotFound() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getRequestForQuoteById(1L));
    }

    @Test
    void updateRequestForQuote_shouldUpdateFields() {
        RequestForQuoteDto dto = RequestForQuoteDto.fromEntity(rfq);
        dto.setTitle("Updated");

        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(rfq));
        when(repository.save(any())).thenReturn(rfq);
        when(attachmentService.getAttachments(any(), any())).thenReturn(List.of());

        RequestForQuoteDetailedDto result =
                service.updateRequestForQuote(1L, dto);

        verify(repository).save(any());
        assertEquals("Updated", rfq.getTitle());
        assertEquals(1L, result.getId());
    }

    @Test
    void uploadAttachment_shouldCallAttachmentService() {
        MultipartFile file = mock(MultipartFile.class);

        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(rfq));
        when(attachmentService.getAttachments(any(), any())).thenReturn(List.of());

        RequestForQuoteDetailedDto result =
                service.uploadAttachment(1L, file);

        verify(attachmentService)
                .createAttachment(1L, SourceType.REQUEST_FOR_QUOTE, file);

        assertEquals(1L, result.getId());
    }

    @Test
    void deleteAttachment_shouldCallAttachmentService() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(rfq));
        when(attachmentService.getAttachments(any(), any())).thenReturn(List.of());

        RequestForQuoteDetailedDto result =
                service.deleteAttachment(1L, "att1");

        verify(attachmentService)
                .deleteAttachment("att1", SourceType.REQUEST_FOR_QUOTE);

        assertEquals(1L, result.getId());
    }

    @Test
    void deleteRequestForQuote_shouldSoftDeleteAndRemoveAttachments() {
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(rfq));

        service.deleteRequestForQuote(1L);

        verify(repository).save(rfq);
        verify(attachmentService)
                .deleteAttachmentsBySourceIdAndSourceType(1L,
                        SourceType.REQUEST_FOR_QUOTE);

        assertFalse(rfq.isActive());
    }

    @Test
    void getAllRequestForQuotesByUser_shouldReturnPagedResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RequestForQuote> page =
                new PageImpl<>(List.of(rfq));

        when(repository.findAllByUserIdAndActiveTrue(1L, pageable))
                .thenReturn(page);

        when(serviceCatalogueService.getAllServicesAsMap(any()))
                .thenReturn(Map.of(1L, new ServiceDto()));

        when(attachmentService.getAttachments(any(), any()))
                .thenReturn(List.of());

        Page<RequestForQuoteDetailedDto> result =
                service.getAllRequestForQuotesByUser(1L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllRequestForQuoteByVendor_shouldReturnPagedResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RequestForQuote> page =
                new PageImpl<>(List.of(rfq));

        when(repository.findAllByVendorIdsContainingAndActiveTrue(1L, pageable))
                .thenReturn(page);

        when(attachmentService.getAttachments(any(), any()))
                .thenReturn(List.of());

        Page<RequestForQuoteDetailedDto> result =
                service.getAllRequestForQuoteByVendor(1L, pageable);

        assertEquals(1, result.getTotalElements());
    }
}
