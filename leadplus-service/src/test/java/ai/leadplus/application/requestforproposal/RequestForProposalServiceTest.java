package ai.leadplus.application.requestforproposal;

import ai.leadplus.application.attachments.AttachmentService;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.services.ServiceCatalogueService;
import ai.leadplus.application.specifications.SpecificationService;
import ai.leadplus.domain.attachments.SourceType;
import ai.leadplus.domain.common.BudgetRange;
import ai.leadplus.domain.common.RequestStatus;
import ai.leadplus.domain.requestforproposal.RequestForProposal;
import ai.leadplus.domain.requestforproposal.RequestForProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestForProposalServiceTest {

    @Mock
    private RequestForProposalRepository repository;

    @Mock
    private ServiceCatalogueService serviceCatalogueService;

    @Mock
    private SpecificationService specificationService;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private RequestForProposalService service;

    private RequestForProposal entity;
    private RequestForProposalDto dto;

    @BeforeEach
    void setUp() {
        entity = RequestForProposal.builder()
                .id(1L)
                .userId(1L)
                .title("Test RFP")
                .serviceIds(List.of(1L))
                .specificationIds(List.of(1L))
                .quantity(5)
                .budget(BudgetRange.BELOW_1K)
                .timeline("2 Weeks")
                .description("Test description")
                .status(RequestStatus.OPEN)
                .active(true)
                .build();

        dto = RequestForProposalDto.fromEntity(entity);
    }

    @Test
    void create_ShouldValidateAndSave() {

        when(repository.save(any(RequestForProposal.class))).thenReturn(entity);

        RequestForProposalDto result = service.createRequestForProposal(dto);

        assertNotNull(result);
        assertEquals(RequestStatus.OPEN, result.getStatus());
        assertTrue(result.isActive());

        verify(serviceCatalogueService).validateServiceIds(dto.getServiceIds());
        verify(specificationService).validateSpecificationIds(dto.getSpecificationIds());
        verify(repository).save(any(RequestForProposal.class));
    }

    @Test
    void getById_ShouldReturnWithAttachments() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(entity));
        when(attachmentService.getAttachments(1L, SourceType.REQUEST_FOR_PROPOSAL))
                .thenReturn(List.of());

        RequestForProposalWithAttachmentsDto result =
                service.getRequestForProposalById(1L);

        assertNotNull(result);
        assertEquals("Test RFP", result.getTitle());

        verify(attachmentService)
                .getAttachments(1L, SourceType.REQUEST_FOR_PROPOSAL);
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getRequestForProposalById(1L));
    }

    @Test
    void getAllByUser_ShouldReturnPagedResult() {

        Page<RequestForProposal> page = new PageImpl<>(List.of(entity));
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAllByUserIdAndActiveTrue(1L, pageable))
                .thenReturn(page);
        when(attachmentService.getAttachments(any(), any()))
                .thenReturn(List.of());

        Page<RequestForProposalWithAttachmentsDto> result =
                service.getAllRequestForProposalsByUser(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test RFP", result.getContent().getFirst().getTitle());
    }

    @Test
    void update_ShouldNotRevalidate_WhenIdsUnchanged() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(entity));
        when(repository.save(any()))
                .thenReturn(entity);
        when(attachmentService.getAttachments(any(), any()))
                .thenReturn(List.of());

        service.updateRequestForProposal(1L, dto);

        verify(serviceCatalogueService, never()).validateServiceIds(any());
        verify(specificationService, never()).validateSpecificationIds(any());
    }

    @Test
    void update_ShouldRevalidate_WhenServiceIdsChanged() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(entity));
        when(repository.save(any()))
                .thenReturn(entity);
        when(attachmentService.getAttachments(any(), any()))
                .thenReturn(List.of());

        dto.setServiceIds(List.of(2L));

        service.updateRequestForProposal(1L, dto);

        verify(serviceCatalogueService).validateServiceIds(dto.getServiceIds());
    }

    @Test
    void update_ShouldRevalidate_WhenSpecificationIdsChanged() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(entity));
        when(repository.save(any()))
                .thenReturn(entity);
        when(attachmentService.getAttachments(any(), any()))
                .thenReturn(List.of());

        dto.setSpecificationIds(List.of(3L));

        service.updateRequestForProposal(1L, dto);

        verify(specificationService)
                .validateSpecificationIds(dto.getSpecificationIds());
    }

    @Test
    void uploadAttachment_ShouldCreateAttachment() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello".getBytes()
        );

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(entity));
        when(attachmentService.getAttachments(any(), any()))
                .thenReturn(List.of());

        service.uploadAttachment(1L, file);

        verify(attachmentService)
                .createAttachment(1L, SourceType.REQUEST_FOR_PROPOSAL, file);
    }

    @Test
    void delete_ShouldSoftDelete() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(entity));

        service.deleteRequestForProposal(1L);

        assertFalse(entity.isActive());
        verify(repository).save(entity);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteRequestForProposal(1L));
    }
}
