package ai.leadplus.application.quotations;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.quotations.Quotation;
import ai.leadplus.domain.quotations.QuotationRepository;
import ai.leadplus.domain.common.SourcingRequestType;
import ai.leadplus.domain.quotations.QuotationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotationServiceTest {

    private static final Long QUOTATION_ID = 1L;
    private static final Long VENDOR_ID = 1L;
    private static final Long SOURCE_ID = 1L;

    @Mock
    private QuotationRepository quotationRepository;

    @InjectMocks
    private QuotationService quotationService;

    private Quotation quotation;
    private SourcingRequestType sourceType;

    @BeforeEach
    void setUp() {
        sourceType = SourcingRequestType.REQUEST_FOR_QUOTE;
        quotation = Quotation.builder()
                .id(QUOTATION_ID)
                .vendorId(VENDOR_ID)
                .sourceId(SOURCE_ID)
                .sourceType(sourceType)
                .status(QuotationStatus.PENDING)
                .active(true)
                .build();
    }

    @Test
    void createQuotation_shouldSaveAndReturnDto() {
        var dto = QuotationDto.fromEntity(quotation);

        when(quotationRepository.save(any(Quotation.class))).thenReturn(quotation);

        var result = quotationService.createQuotation(dto, sourceType);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(QuotationStatus.PENDING);
        assertThat(result.getSourceType()).isEqualTo(sourceType);

        verify(quotationRepository).save(any(Quotation.class));
    }

    @Test
    void getQuotationById_shouldReturnDto_whenExists() {
        when(quotationRepository.findByIdAndActiveTrue(QUOTATION_ID))
                .thenReturn(Optional.of(quotation));

        var result = quotationService.getQuotationById("1");

        assertThat(result.getId()).isEqualTo(QUOTATION_ID);
    }

    @Test
    void getQuotationById_shouldThrowException_whenNotFound() {
        when(quotationRepository.findByIdAndActiveTrue(QUOTATION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> quotationService.getQuotationById("1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quotation not found");
    }

    @Test
    void getQuotationByIdAndSourceType_shouldReturnDto() {
        when(quotationRepository.findByIdAndSourceTypeAndActiveTrue(QUOTATION_ID, sourceType))
                .thenReturn(Optional.of(quotation));

        var result = quotationService.getQuotationByIdAndSourceType("1", sourceType);

        assertThat(result.getId()).isEqualTo(QUOTATION_ID);
    }

    @Test
    void getAllByVendorIdAndSourceType_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Quotation> quotationPage = new PageImpl<>(List.of(quotation), pageable, 1);

        when(quotationRepository
                .findAllByVendorIdAndSourceTypeAndActiveTrue(VENDOR_ID, sourceType, pageable))
                .thenReturn(quotationPage);

        Page<QuotationDto> result = quotationService.getAllByVendorIdAndSourceType(VENDOR_ID, sourceType, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getVendorId()).isEqualTo(VENDOR_ID);
    }

    @Test
    void getAllBySource_shouldReturnList() {
        when(quotationRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(SOURCE_ID, sourceType))
                .thenReturn(List.of(quotation));

        var result = quotationService.getAllBySource(SOURCE_ID, sourceType);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSourceId()).isEqualTo(SOURCE_ID);
    }

    @Test
    void getAllBySourceIdAndSourceType_shouldReturnPage() {
        var pageable = PageRequest.of(0, 10);
        when(quotationRepository.findAllBySourceIdAndSourceTypeAndActiveTrue(SOURCE_ID, sourceType, pageable))
                .thenReturn(new PageImpl<>(List.of(quotation)));

        Page<QuotationDto> result = quotationService.getAllBySourceIdAndSourceType(SOURCE_ID, sourceType, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateQuotation_shouldUpdatePrice() {
        var updateDto = QuotationDto.fromEntity(quotation);

        when(quotationRepository.findByIdAndVendorIdAndSourceTypeAndActiveTrue(QUOTATION_ID, VENDOR_ID, sourceType))
                .thenReturn(Optional.of(quotation));
        when(quotationRepository.save(any(Quotation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = quotationService.updateQuotation(1L, updateDto, sourceType);

        verify(quotationRepository).save(quotation);
    }

    @Test
    void updateQuotationStatus_shouldUpdateStatus() {
        when(quotationRepository.findByIdAndVendorIdAndSourceTypeAndActiveTrue(QUOTATION_ID, VENDOR_ID, sourceType))
                .thenReturn(Optional.of(quotation));
        when(quotationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = quotationService.updateQuotationStatus(1L, VENDOR_ID, QuotationStatus.ACCEPTED, sourceType);

        assertThat(result.getStatus()).isEqualTo(QuotationStatus.ACCEPTED);
        verify(quotationRepository).save(quotation);
    }

    @Test
    void deleteQuotation_shouldSetActiveFalse() {
        when(quotationRepository.findByIdAndVendorIdAndSourceTypeAndActiveTrue(QUOTATION_ID, VENDOR_ID, sourceType))
                .thenReturn(Optional.of(quotation));

        quotationService.deleteQuotation(1L, VENDOR_ID, sourceType);

        assertThat(quotation.isActive()).isFalse();
        verify(quotationRepository).save(quotation);
    }

    @Test
    void updateQuotation_shouldThrowException_whenNotFound() {
        var updateDto = QuotationDto.fromEntity(quotation);

        when(quotationRepository.findByIdAndVendorIdAndSourceTypeAndActiveTrue(QUOTATION_ID, VENDOR_ID, sourceType))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> quotationService.updateQuotation(1L, updateDto, sourceType))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quotation not found");
    }
}
